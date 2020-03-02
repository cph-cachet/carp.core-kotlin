package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.UUID
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
class StudyDeployment( val protocolSnapshot: StudyProtocolSnapshot, val id: UUID = UUID.randomUUID() )
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: StudyDeploymentSnapshot ): StudyDeployment
        {
            val deployment = StudyDeployment( snapshot.studyProtocolSnapshot, snapshot.studyDeploymentId )

            // Add registered devices.
            snapshot.registeredDevices.forEach { r ->
                val registrable = deployment.registrableDevices.firstOrNull { it.device.roleName == r.key }
                    ?: throw IllegalArgumentException( "Can't find registered device with role name '${r.key}' in snapshot." )
                deployment.registerDevice( registrable.device, r.value )
            }

            // Add participations.
            snapshot.participations.forEach { p ->
                deployment._participations.add( AccountParticipation( p.accountId, p.participationId ) )
            }

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
     * The set of devices which have already been registered for this study deployment.
     */
    val registeredDevices: Map<AnyDeviceDescriptor, DeviceRegistration>
        get() = _registeredDevices

    private val _registeredDevices: MutableMap<AnyDeviceDescriptor, DeviceRegistration> = mutableMapOf()

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
            // Top-level master devices require registration.
            .map { RegistrableDevice( it, isTopLevelMasterDevice( it ) ) }
            .toMutableSet()
    }


    /**
     * Get the status (serializable) of this [StudyDeployment].
     */
    fun getStatus(): StudyDeploymentStatus
    {
        val devicesStatus: List<DeviceDeploymentStatus> =
            _registrableDevices.map {
                val isRegistered = _registeredDevices.contains( it.device )
                val requiresDeployment = isTopLevelMasterDevice( it.device )
                val isReadyForDeployment = canObtainDeviceDeployment( it.device )
                val isDeployed = false // TODO: For now, deployment manager is not yet notified of successful deployment.
                DeviceDeploymentStatus( it.device, it.requiresRegistration, isRegistered, requiresDeployment, isReadyForDeployment, isDeployed )
            }

        return StudyDeploymentStatus( id, devicesStatus )
    }

    private fun isTopLevelMasterDevice( device: AnyDeviceDescriptor ): Boolean =
        device is AnyMasterDeviceDescriptor && protocol.masterDevices.contains( device )

    /**
     * Determines whether the deployment configuration (to initialize the device environment) for a specific device can be obtained.
     * This requires the specified device and all other master devices it depends on to be registered.
     */
    private fun canObtainDeviceDeployment( device: AnyDeviceDescriptor ): Boolean
    {
        val requiresDeployment = isTopLevelMasterDevice( device )
        val allRequiredDevicesRegistered = _registrableDevices
            .filter { it.requiresRegistration }
            .map { it.device }
            .minus( registeredDevices.keys )
            .isEmpty()

        // TODO: For now, presume all devices which require registration may depend on one another.
        //       This can be optimized by looking at the triggers which determine actual dependencies between devices.
        return requiresDeployment && allRequiredDevicesRegistered
    }

    /**
     * Register the specified [device] for this deployment using the passed [registration] options.
     */
    fun registerDevice( device: AnyDeviceDescriptor, registration: DeviceRegistration )
    {
        val containsDevice: Boolean = _registrableDevices.any { it.device == device }
        require( containsDevice ) { "The passed device is not part of this deployment." }

        // TODO: For now, given that we don't fully know requirements of changing registration of devices yet, do not allow it.
        val isAlreadyRegistered = _registeredDevices.keys.contains( device )
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

        _registeredDevices[ device ] = registration
    }

    /**
     * Get the deployment configuration for the specified [device] in this study deployment.
     *
     * @throws IllegalArgumentException when the passed [device] is not part of the protocol of this study deployment.
     * @throws IllegalArgumentException when the passed [device] is not ready to receive a [MasterDeviceDeployment] yet.
     */
    fun getDeviceDeploymentFor( device: AnyMasterDeviceDescriptor ): MasterDeviceDeployment
    {
        // Verify whether the specified device is part of the protocol of this deployment.
        require( protocolSnapshot.masterDevices.contains( device ) ) { "The specified master device is not part of the protocol of this deployment." }

        // Verify whether the specified device is ready to be deployed.
        val canDeploy = canObtainDeviceDeployment( device )
        require( canDeploy ) { "The specified device is awaiting registration of itself or other devices before it can be deployed." }

        val configuration: DeviceRegistration = _registeredDevices[ device ]!! // Must be non-null, otherwise canObtainDeviceDeployment would fail.

        // Determine which devices this device needs to connect to and retrieve configuration for preregistered devices.
        val connectedDevices: Set<AnyDeviceDescriptor> = protocol.getConnectedDevices( device ).toSet()
        val deviceRegistrations: Map<String, DeviceRegistration> = _registeredDevices
            .filter { connectedDevices.contains( it.key ) }
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
            .filter { relevantDeviceRoles.contains( it.value.sourceDeviceRoleName ) }
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
     * Add [participation] details for a given [account] to this study deployment.
     *
     * @throws IllegalArgumentException if the specified [account] already participates in this deployment,
     * or if the [participation] details do not match this study deployment.
     */
    fun addParticipation( account: Account, participation: Participation )
    {
        require( id == participation.studyDeploymentId ) { "The specified participation details do not match this study deployment." }
        require( _participations.none { it.accountId == account.id } ) { "The specified account already participates in this study deployment." }

        _participations.add( AccountParticipation( account.id, participation.id ) )
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
    fun getSnapshot(): StudyDeploymentSnapshot
    {
        return StudyDeploymentSnapshot.fromDeployment( this )
    }
}
