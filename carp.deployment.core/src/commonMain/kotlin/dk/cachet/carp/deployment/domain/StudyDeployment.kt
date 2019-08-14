package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*


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
            val deployment = StudyDeployment( snapshot.studyProtocolSnapshot, snapshot.deploymentId )

            // Add registered devices.
            snapshot.registeredDevices.forEach { r ->
                val registrable = deployment.registrableDevices.firstOrNull { it.device.roleName == r.key }
                    ?: throw IllegalArgumentException( "Can't find registered device with role name '${r.key}' in snapshot." )
                deployment.registerDevice( registrable.device, r.value )
            }

            return deployment
        }
    }


    private val _protocol: StudyProtocol = try
    {
        StudyProtocol.fromSnapshot( protocolSnapshot )
    }
    catch( e: InvalidConfigurationError )
    {
        throw IllegalArgumentException( "Invalid protocol snapshot passed." )
    }

    /**
     * The set of all devices which can or need to be registered for this deployment.
     */
    val registrableDevices: Set<RegistrableDevice>
        get() = _registrableDevices

    private val _registrableDevices: MutableSet<RegistrableDevice>

    /**
     * The set of devices which have already been registered for this deployment.
     */
    val registeredDevices: Map<AnyDeviceDescriptor, DeviceRegistration>
        get() = _registeredDevices

    private val _registeredDevices: MutableMap<AnyDeviceDescriptor, DeviceRegistration> = mutableMapOf()

    init
    {
        require( _protocol.isDeployable() ) { "The passed protocol snapshot contains deployment errors." }

        // Initialize information which devices can or should be registered for this deployment.
        _registrableDevices = _protocol.devices.asSequence()
            // Top-level master devices require registration.
            .map { RegistrableDevice( it, _protocol.masterDevices.contains( it ) ) }
            .toMutableSet()
    }


    /**
     * Get the status (serializable) of this [StudyDeployment].
     */
    fun getStatus(): DeploymentStatus
    {
        val remainingRegistration: Set<String> = getRemainingDevicesToRegister().map { it.roleName }.toSet()
        val devicesReadyForDeployment: Set<String> = _registrableDevices
            .filter {
                it.device is AnyMasterDeviceDescriptor && // Only master devices can be deployed.
                canObtainDeviceDeployment( it.device ) }
            .map { it.device.roleName }
            .toSet()

        return DeploymentStatus(
            id,
            registrableDevices,
            remainingRegistration,
            devicesReadyForDeployment )
    }

    /**
     * Get the subset of registrable devices which require registration and are not yet registered.
     */
    private fun getRemainingDevicesToRegister(): Set<AnyDeviceDescriptor>
    {
        return _registrableDevices
            .asSequence()
            .filter { it.requiresRegistration }
            .map { it.device }
            .minus( registeredDevices.keys )
            .toSet()
    }

    /**
     * Determines whether the deployment configuration (to initialize the device environment) for a specific device can be obtained.
     * This requires the specified device and all other master devices it depends on to be registered.
     */
    private fun canObtainDeviceDeployment( device: AnyMasterDeviceDescriptor ): Boolean
    {
        // TODO: For now, presume all devices which require registration may depend on one another.
        //       This can be optimized by looking at the triggers which determine actual dependencies between devices.
        return getRemainingDevicesToRegister().isEmpty()
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
     * Get the deployment configuration for the specified [device] in this deployment.
     *
     * @throws IllegalArgumentException when the passed [device] is not part of the protocol of this deployment.
     * @throws IllegalArgumentException when the passed [device] is not ready to receive a [DeviceDeployment] yet.
     */
    fun getDeploymentFor( device: AnyMasterDeviceDescriptor ): DeviceDeployment
    {
        // Verify whether the specified device is part of the protocol of this deployment.
        require( protocolSnapshot.masterDevices.contains( device ) ) { "The specified master device is not part of the protocol of this deployment." }

        // Verify whether the specified device is ready to be deployed.
        val canDeploy = canObtainDeviceDeployment( device )
        require( canDeploy ) { "The specified device is awaiting registration of itself or other devices before it can be deployed." }

        val configuration: DeviceRegistration = _registeredDevices[ device ]!! // Must be non-null, otherwise canObtainDeviceDeployment would fail.

        // Determine which devices this device needs to connect to and retrieve configuration for preregistered devices.
        val connectedDevices: Set<AnyDeviceDescriptor> = _protocol.getConnectedDevices( device ).toSet()
        val deviceRegistrations: Map<String, DeviceRegistration> = _registeredDevices
            .filter { connectedDevices.contains( it.key ) }
            .mapKeys { it.key.roleName }

        // Get all tasks which might need to be executed on this or connected devices.
        val relevantDevices = arrayOf( device ).union( connectedDevices )
        val tasks = relevantDevices
            .flatMap { _protocol.getTasksForDevice( it ) }
            .toSet()

        // Get all trigger information for this and connected devices.
        // The trigger IDs assigned by snapshot are reused to identify them within the protocol.
        val relevantDeviceRoles = relevantDevices.map { it.roleName }
        val usedTriggers = protocolSnapshot.triggers
            .filter { relevantDeviceRoles.contains( it.value.sourceDeviceRoleName ) }
        val triggeredTasks = usedTriggers
            .map { it to _protocol.getTriggeredTasks( it.value ) }
            .flatMap { pair -> pair.second.map {
                DeviceDeployment.TriggeredTask( pair.first.key, it.task.name, it.targetDevice.roleName ) } }
            .toSet()

        return DeviceDeployment(
            configuration,
            connectedDevices,
            deviceRegistrations,
            tasks,
            usedTriggers,
            triggeredTasks )
    }


    /**
     * Get a serializable snapshot of the current state of this [StudyDeployment].
     */
    fun getSnapshot(): StudyDeploymentSnapshot
    {
        return StudyDeploymentSnapshot.fromDeployment( this )
    }
}