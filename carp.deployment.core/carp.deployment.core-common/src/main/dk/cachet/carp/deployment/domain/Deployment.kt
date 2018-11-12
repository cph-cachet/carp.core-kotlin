package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*


/**
 * A single instantiation of a [StudyProtocol], taking care of common concerns when 'running' a study.
 *
 * I.e., a [Deployment] is responsible for registering the physical devices described in the [StudyProtocol],
 * enabling a connection between them, tracking device connection issues, assessing data quality,
 * and registering participant consent.
 */
class Deployment( val protocolSnapshot: StudyProtocolSnapshot, val id: UUID = UUID.randomUUID() )
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: DeploymentSnapshot ): Deployment
        {
            val deployment = Deployment( snapshot.studyProtocolSnapshot, UUID( snapshot.deploymentId ) )

            // Add registered devices.
            snapshot.registeredDevices.forEach { r ->
                val registrable = deployment.registrableDevices.firstOrNull { it.device.roleName == r.key }
                    ?: throw IllegalArgumentException( "Can't find registered device with role name '${r.key}' in snapshot." )
                deployment.registerDevice( registrable.device, r.value )
            }

            return deployment
        }
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
    val registeredDevices: Map<DeviceDescriptor, DeviceRegistration>
        get() = _registeredDevices

    private val _registeredDevices: MutableMap<DeviceDescriptor, DeviceRegistration> = mutableMapOf()

    init
    {
        // Verify whether protocol can be deployed.
        val protocol = try
        {
            StudyProtocol.fromSnapshot( protocolSnapshot )
        }
        catch( e: InvalidConfigurationError )
        {
            throw IllegalArgumentException( "Invalid protocol snapshot passed." )
        }
        if ( !protocol.isDeployable() )
        {
            throw IllegalArgumentException( "The passed protocol snapshot contains deployment errors." )
        }

        // Initialize information which devices can or should be registered for this deployment.
        _registrableDevices = protocol.devices.asSequence()
            // Top-level master devices require registration.
            .map { it ->  RegistrableDevice( it, protocol.masterDevices.contains( it ) ) }
            .toMutableSet()
    }


    /**
     * Get the status (serializable) of this [Deployment].
     */
    fun getStatus(): DeploymentStatus
    {
        val remainingRegistration: Set<String> = getRemainingDevicesToRegister().map { it.roleName }.toSet()
        val devicesReadyForDeployment: Set<String> = _registrableDevices
            .filter {
                it.device is MasterDeviceDescriptor && // Only master devices can be deployed.
                canObtainDeviceDeployment( it.device ) }
            .map { it.device.roleName }
            .toSet()

        return DeploymentStatus(
            id.toString(),
            registrableDevices,
            remainingRegistration,
            devicesReadyForDeployment )
    }

    /**
     * Get the subset of registrable devices which require registration and are not yet registered.
     */
    private fun getRemainingDevicesToRegister(): Set<DeviceDescriptor>
    {
        return _registrableDevices
            .asSequence()
            .filter { it.requiresRegistration }
            .map { it.device }
            .minus( registeredDevices.keys )
            .toSet()
    }

    /**
     * Determines whether the deployment configuration for a specific device can be obtained.
     * In case the device for which the deployment configuration is requested depends on another device which is not yet registered,
     * the deployment configuration (to initialize the device environment) cannot be obtained.
     */
    private fun canObtainDeviceDeployment( device: MasterDeviceDescriptor ): Boolean
    {
        // TODO: For now, presume all devices which require registration may depend on one another.
        //       This can be optimized by looking at the triggers which determine actual dependencies between devices.
        return getRemainingDevicesToRegister().isEmpty()
    }

    /**
     * Register the specified [device] for this deployment using the passed [registration] options.
     */
    fun registerDevice( device: DeviceDescriptor, registration: DeviceRegistration )
    {
        val containsDevice: Boolean = _registrableDevices.any { it.device == device }
        if ( !containsDevice )
        {
            throw IllegalArgumentException( "The passed device is not part of this deployment." )
        }

        val isAlreadyRegistered = _registeredDevices.keys.contains( device )
        if ( isAlreadyRegistered )
        {
            // TODO: For now, given that we don't fully know requirements of changing registration of devices yet, do not allow it.
            throw IllegalArgumentException( "The passed device is already registered." )
        }

        // Verify whether the passed registration is known to be invalid for the given device.
        // This may be 'UNKNOWN' when the device type is not known at runtime.
        // In this case, simply forward as is assuming it to be valid (possibly failing in 'environment' later).
        val registrationCopy = registration.copy() // Copy registration to prevent external modification.
        if ( device.isValidConfiguration( registrationCopy ) == Trilean.FALSE )
        {
            throw IllegalArgumentException( "The passed registration is not valid for the given device." )
        }

        // Verify whether deviceId is unique for the given device type within this deployment.
        val isUnique = _registeredDevices.none {
            val isSameId = it.value.deviceId == registrationCopy.deviceId
            val otherDevice = it.key
            val areUnknownDevices = device is UnknownPolymorphicWrapper && otherDevice is UnknownPolymorphicWrapper
            val matchingUnknownDevices: Boolean by lazy { (device as UnknownPolymorphicWrapper).className == (otherDevice as UnknownPolymorphicWrapper).className }
            isSameId && ( (!areUnknownDevices && otherDevice::class == device::class) || (areUnknownDevices && matchingUnknownDevices) ) }
        if ( !isUnique )
        {
            throw IllegalArgumentException(
                "The deviceId specified in the passed registration is already in use by a device of the same type. " +
                "Cannot register the same device for different device roles within a deployment." )
        }

        _registeredDevices[ device ] = registrationCopy
    }


    /**
     * Get a serializable snapshot of the current state of this [Deployment].
     */
    fun getSnapshot(): DeploymentSnapshot
    {
        return DeploymentSnapshot.fromDeployment( this )
    }
}