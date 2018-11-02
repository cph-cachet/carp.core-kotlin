package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*


/**
 * A single instantiation of a [StudyProtocol], taking care of common concerns when 'running' a study.
 *
 * I.e., a [Deployment] is responsible for registering the physical devices described in the [StudyProtocol],
 * enabling a connection between them, tracking device connection issues, assessing data quality,
 * and registering participant consent.
 */
class Deployment( protocolSnapshot: StudyProtocolSnapshot, val id: UUID = UUID.randomUUID() )
{
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
        val remainingRegistration: Set<String> = _registrableDevices
            .asSequence()
            .filter { it.requiresRegistration }
            .map { it.device.roleName }
            .minus( registeredDevices.keys.map { it.roleName } )
            .toSet()

        return DeploymentStatus( id.toString(), registrableDevices, remainingRegistration )
    }
}