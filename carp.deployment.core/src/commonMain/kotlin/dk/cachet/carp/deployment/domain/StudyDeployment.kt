package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.AccountParticipation
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * A single instantiation of a [StudyProtocol], taking care of common concerns when 'running' a study.
 *
 * I.e., a [StudyDeployment] is responsible for registering the physical devices described in the [StudyProtocol],
 * enabling a connection between them, tracking device connection issues, assessing data quality,
 * and registering participant consent.
 */
class StudyDeployment( val protocolSnapshot: StudyProtocolSnapshot, val id: UUID = UUID.randomUUID() ) :
    AggregateRoot<StudyDeployment, StudyDeploymentSnapshot, StudyDeployment.Event>()
{
    sealed class Event : Immutable()
    {
        data class DeviceRegistered( val device: AnyDeviceDescriptor, val registration: DeviceRegistration ) : Event()
        data class DeviceUnregistered( val device: AnyDeviceDescriptor ) : Event()
        data class DeviceDeployed( val device: AnyMasterDeviceDescriptor ) : Event()
        data class DeploymentInvalidated( val device: AnyMasterDeviceDescriptor ) : Event()
        data class ParticipationAdded( val accountParticipation: AccountParticipation ) : Event()
    }


    companion object Factory
    {
        fun fromSnapshot( snapshot: StudyDeploymentSnapshot ): StudyDeployment
        {
            val deployment = StudyDeployment( snapshot.studyProtocolSnapshot, snapshot.studyDeploymentId )

            // Replay device registration history.
            snapshot.deviceRegistrationHistory.forEach { r ->
                val registrable = deployment.registrableDevices.firstOrNull { it.device.roleName == r.key }
                    ?: throw IllegalArgumentException( "Can't find registered device with role name '${r.key}' in snapshot." )
                r.value.forEach { deployment.registerDevice( registrable.device, it ) }

                // In case snapshot indicates the device is currently not registered, unregister it.
                if ( r.key !in snapshot.registeredDevices )
                {
                    deployment.unregisterDevice( registrable.device )
                }
            }

            // Add deployed devices.
            snapshot.deployedDevices.forEach { roleName ->
                val deployedDevice = deployment.protocolSnapshot.masterDevices.firstOrNull { it.roleName == roleName }
                    ?: throw IllegalArgumentException( "Can't find deployed device with role name '$roleName' in snapshot." )
                val deviceDeployment = deployment.getDeviceDeploymentFor( deployedDevice )
                deployment.deviceDeployed( deployedDevice, deviceDeployment.getChecksum() )
            }

            // Add invalidated deployed devices.
            val invalidatedDevices = snapshot.invalidatedDeployedDevices.map { invalidatedRoleName ->
                deployment.protocolSnapshot.masterDevices.firstOrNull { it.roleName == invalidatedRoleName }
                    ?: throw IllegalArgumentException( "Can't find deployed device with role name '$invalidatedRoleName' in snapshot." )
            }
            deployment._invalidatedDeployedDevices.addAll( invalidatedDevices )

            // Add participations.
            snapshot.participations.forEach { p ->
                deployment._participations.add( AccountParticipation( p.accountId, p.participationId ) )
            }

            // Events introduced by loading the snapshot are not relevant to a consumer wanting to persist changes.
            deployment.consumeEvents()

            return deployment
        }
    }


    val protocol: StudyProtocol =
        try
        {
            StudyProtocol.fromSnapshot( protocolSnapshot )
        }
        catch ( e: InvalidConfigurationError )
        {
            throw IllegalArgumentException( "Invalid protocol snapshot passed." )
        }

    /**
     * The set of all devices which can or need to be registered for this study deployment.
     */
    val registrableDevices: Set<RegistrableDevice>
        get() = _registrableDevices

    private val _registrableDevices: MutableSet<RegistrableDevice>

    /**
     * The set of devices which are currently registered for this study deployment.
     */
    val registeredDevices: Map<AnyDeviceDescriptor, DeviceRegistration>
        get() = _registeredDevices

    private val _registeredDevices: MutableMap<AnyDeviceDescriptor, DeviceRegistration> = mutableMapOf()

    /**
     * Per device, a list of all device registrations (included old registrations) in the order they were registered.
     */
    val deviceRegistrationHistory: Map<AnyDeviceDescriptor, List<DeviceRegistration>>
        get() = _deviceRegistrationHistory

    private val _deviceRegistrationHistory: MutableMap<AnyDeviceDescriptor, MutableList<DeviceRegistration>> = mutableMapOf()

    /**
     * The set of devices which have been deployed correctly.
     */
    val deployedDevices: Set<AnyMasterDeviceDescriptor>
        get() = _deployedDevices

    private val _deployedDevices: MutableSet<AnyMasterDeviceDescriptor> = mutableSetOf()

    /**
     * Devices which have been previously deployed correctly, but due to changes in device registrations need to be redeployed.
     */
    val invalidatedDeployedDevices: Set<AnyMasterDeviceDescriptor>
        get() = _invalidatedDeployedDevices

    private val _invalidatedDeployedDevices: MutableSet<AnyMasterDeviceDescriptor> = mutableSetOf()

    /**
     * The account IDs participating in this study deployment and the pseudonym IDs assigned to them.
     */
    val participations: Set<AccountParticipation>
        get() = _participations

    private val _participations: MutableSet<AccountParticipation> = mutableSetOf()

    init
    {
        require( protocol.isDeployable() ) { "The passed protocol snapshot contains deployment errors." }

        // Initialize information which devices can or should be registered for this deployment.
        _registrableDevices = protocol.devices
            // Top-level master devices require deployment.
            .map { RegistrableDevice( it, it in protocol.masterDevices ) }
            .toMutableSet()
    }


    /**
     * Get the status (serializable) of this [StudyDeployment].
     */
    fun getStatus(): StudyDeploymentStatus
    {
        val devicesStatus: List<DeviceDeploymentStatus> = _registrableDevices.map { getDeviceStatus( it.device ) }
        val allDevicesDeployed: Boolean = devicesStatus.all { it is DeviceDeploymentStatus.Deployed }

        return when {
            allDevicesDeployed -> StudyDeploymentStatus.DeploymentReady( id, devicesStatus )
            else -> StudyDeploymentStatus.DeployingDevices( id, devicesStatus )
        }
    }

    /**
     * Get the status of a device in this [StudyDeployment].
     */
    private fun getDeviceStatus( device: AnyDeviceDescriptor ): DeviceDeploymentStatus
    {
        val registrableDevice = registrableDevices.first{ it.device == device }

        val isRegistered = device in _registeredDevices
        val toRegisterBeforeDeployment = getDependentDevices( device ).plus( device )
            .map { d -> d.roleName }
            .minus( registeredDevices.keys.map { r -> r.roleName } )
            .toSet()
        val requiresDeployment = registrableDevice.requiresDeployment
        val isDeployed = device in deployedDevices
        val needsRedeployment = device in invalidatedDeployedDevices

        return when
        {
            needsRedeployment -> DeviceDeploymentStatus.NeedsRedeployment( device, toRegisterBeforeDeployment )
            isDeployed -> DeviceDeploymentStatus.Deployed( device )
            isRegistered -> DeviceDeploymentStatus.Registered( device, requiresDeployment, toRegisterBeforeDeployment )
            else -> DeviceDeploymentStatus.Unregistered( device, requiresDeployment, toRegisterBeforeDeployment )
        }
    }

    /**
     * Get all devices which need to be registered before the passed [device] can be deployed.
     *
     * TODO: For now, presume all devices which require deployment may depend on one another.
     *       This can be optimized by looking at the triggers which determine actual dependencies between devices.
     */
    private fun getDependentDevices( device: AnyDeviceDescriptor ): List<AnyDeviceDescriptor> =
        when ( device )
        {
            is AnyMasterDeviceDescriptor ->
                _registrableDevices
                    .filter { it.requiresDeployment }
                    .map { it.device }
                    .minus( device )
            else -> emptyList() // Only master devices can be deployed. Other devices have no 'dependent' devices.
        }

    /**
     * Register the specified [device] for this deployment using the passed [registration] options.
     */
    fun registerDevice( device: AnyDeviceDescriptor, registration: DeviceRegistration )
    {
        val containsDevice: Boolean = _registrableDevices.any { it.device == device }
        require( containsDevice ) { "The passed device is not part of this deployment." }

        val isAlreadyRegistered = device in _registeredDevices.keys
        require( !isAlreadyRegistered ) { "The passed device is already registered." }

        // Verify whether the passed registration is known to be invalid for the given device.
        // This may be 'UNKNOWN' when the device type is not known at runtime.
        // In this case, simply forward as is assuming it to be valid (possibly failing on the 'client' later).
        // TODO: `getRegistrationClass` is a trivial implementation in extending classes, but could this be enforced by using the type system instead?
        //       On the JVM runtime, `isValidConfiguration` throws a `ClassCastException` when the wrong type were to be passed, but not on JS runtime.
        val registrationClass = device.getRegistrationClass()
        val isValidType = registrationClass.isInstance( registration )
        @Suppress( "UNCHECKED_CAST" )
        val anyDevice = device as DeviceDescriptor<DeviceRegistration, *>
        val isValidConfiguration = isValidType && ( anyDevice.isValidConfiguration( registration ) != Trilean.FALSE )
        require( isValidConfiguration ) { "The passed registration is not valid for the given device." }

        // Verify whether deviceId is unique for the given device type within this deployment.
        val isUnique = _registeredDevices.none {
            val isSameId = it.value.deviceId == registration.deviceId
            val otherDevice = it.key
            val areUnknownDevices = device is UnknownPolymorphicWrapper && otherDevice is UnknownPolymorphicWrapper
            val matchingUnknownDevices: Boolean by lazy { (device as UnknownPolymorphicWrapper).className == (otherDevice as UnknownPolymorphicWrapper).className }
            isSameId && ( (!areUnknownDevices && otherDevice::class == device::class) || (areUnknownDevices && matchingUnknownDevices) ) }
        require( isUnique ) {
            "The deviceId specified in the passed registration is already in use by a device of the same type. " +
            "Cannot register the same device for different device roles within a deployment." }

        // Add device to currently registered devices, but also store it in registration history.
        _registeredDevices[ device ] = registration
        val registrationHistory = _deviceRegistrationHistory.getOrPut( device ) { mutableListOf() }
        registrationHistory.add( registration )
        event( Event.DeviceRegistered( device, registration ) )
    }

    /**
     * Remove the current device registration for the [device] in this deployment.
     * This will invalidate the deployment of any devices which depend on the this [device].
     */
    fun unregisterDevice( device: AnyDeviceDescriptor )
    {
        val containsDevice: Boolean = _registrableDevices.any { it.device == device }
        require( containsDevice ) { "The passed device is not part of this deployment." }
        require( device in _registeredDevices ) { "The passed device is not registered for this deployment." }

        _registeredDevices.remove( device )
        _deployedDevices.remove( device )

        event( Event.DeviceUnregistered( device ) )

        // Invalidate deployed master devices which depend on this device that are deployed.
        val dependentMasterDevices = getDependentDevices( device )
            .filterIsInstance<AnyMasterDeviceDescriptor>()
        dependentMasterDevices.forEach {
            if ( _deployedDevices.remove( it ) )
            {
                _invalidatedDeployedDevices.add( it )
                event( Event.DeploymentInvalidated( it ) )
            }
        }
    }

    /**
     * Get the deployment configuration for the specified [device] in this study deployment.
     *
     * @throws IllegalArgumentException when the passed [device] is not part of the protocol of this study deployment.
     * @throws IllegalStateException when a [MasterDeviceDeployment] for the passed [device] is not yet available.
     */
    fun getDeviceDeploymentFor( device: AnyMasterDeviceDescriptor ): MasterDeviceDeployment
    {
        // Verify whether the specified device is part of the protocol of this deployment.
        require( device in protocolSnapshot.masterDevices ) { "The specified master device is not part of the protocol of this deployment." }

        // Verify whether the specified device is ready to be deployed.
        val canDeploy = getDeviceStatus( device ).canObtainDeviceDeployment
        check( canDeploy ) { "The specified device is awaiting registration of itself or other devices before it can be deployed." }

        val configuration: DeviceRegistration = _registeredDevices[ device ]!! // Must be non-null, otherwise canObtainDeviceDeployment would fail.

        // Determine which devices this device needs to connect to and retrieve configuration for preregistered devices.
        val connectedDevices: Set<AnyDeviceDescriptor> = protocol.getConnectedDevices( device ).toSet()
        val deviceRegistrations: Map<String, DeviceRegistration> = _registeredDevices
            .filter { it.key in connectedDevices }
            .mapKeys { it.key.roleName }

        // Get all tasks which might need to be executed on this or connected devices.
        val relevantDevices = arrayOf( device ).union( connectedDevices )
        val tasks = relevantDevices
            .flatMap { protocol.getTasksForDevice( it ) }
            .toSet()

        // Get all trigger information for this and connected devices.
        // The trigger IDs assigned by snapshot are reused to identify them within the protocol.
        val relevantDeviceRoles = relevantDevices.map { it.roleName }
        val usedTriggers = protocolSnapshot.triggers
            .filter { it.value.sourceDeviceRoleName in relevantDeviceRoles }
        val triggeredTasks = usedTriggers
            .map { it to protocol.getTriggeredTasks( it.value ) }
            .flatMap { pair -> pair.second.map {
                MasterDeviceDeployment.TriggeredTask( pair.first.key, it.task.name, it.targetDevice.roleName ) } }
            .toSet()

        return MasterDeviceDeployment(
            configuration,
            connectedDevices,
            deviceRegistrations,
            tasks,
            usedTriggers,
            triggeredTasks )
    }

    /**
     * Indicate that the specified [device] was deployed successfully using the deployment with the specified [deploymentChecksum].
     *
     * @throws IllegalArgumentException when:
     * - the passed [device] is not part of the protocol of this study deployment
     * - the [deploymentChecksum] does not match the checksum of the expected deployment. The deployment might be outdated.
     * @throws IllegalStateException when the passed [device] cannot be deployed yet.
     */
    fun deviceDeployed( device: AnyMasterDeviceDescriptor, deploymentChecksum: Int )
    {
        // Verify whether the specified device is part of the protocol of this deployment.
        require( device in protocolSnapshot.masterDevices ) { "The specified master device is not part of the protocol of this deployment." }

        // Verify whether deployment matches the expected deployment.
        val latestDeployment = getDeviceDeploymentFor( device )
        require( latestDeployment.getChecksum() == deploymentChecksum )

        // Verify whether the specified device is ready to be deployed.
        val canDeploy = getDeviceStatus( device ).canObtainDeviceDeployment
        check( canDeploy ) { "The specified device is awaiting registration of itself or other devices before it can be deployed." }

        if ( _deployedDevices.add( device ) )
        {
            event( Event.DeviceDeployed( device ) )
        }
    }

    /**
     * Add [participation] details for a given [account] to this study deployment.
     *
     * @throws IllegalArgumentException if the specified [account] already participates in this deployment,
     * or if the [participation] details do not match this study deployment.
     */
    fun addParticipation( account: Account, participation: Participation )
    {
        require( id == participation.studyDeploymentId ) { "The specified participation details do not match this study deployment." }
        require( _participations.none { it.accountId == account.id } ) { "The specified account already participates in this study deployment." }

        val accountParticipation = AccountParticipation( account.id, participation.id )
        _participations.add( accountParticipation )
        event( Event.ParticipationAdded( accountParticipation ) )
    }

    /**
     * Get the participation details for a given [account] in this study deployment,
     * or null in case the [account] does not participate in this study deployment.
     */
    fun getParticipation( account: Account ): Participation? =
        _participations
            .filter { it.accountId == account.id }
            .map { Participation( id, it.participationId ) }
            .singleOrNull()


    /**
     * Get a serializable snapshot of the current state of this [StudyDeployment].
     */
    override fun getSnapshot(): StudyDeploymentSnapshot = StudyDeploymentSnapshot.fromDeployment( this )
}
