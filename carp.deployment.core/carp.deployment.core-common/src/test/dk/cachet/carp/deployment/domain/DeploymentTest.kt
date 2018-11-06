package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.json.JSON
import kotlin.test.*


/**
 * Tests for [Deployment].
 */
class DeploymentTest
{
    @Test
    fun cant_initialize_deployment_with_errors()
    {
        val protocol = createEmptyProtocol()
        val snapshot = protocol.getSnapshot()

        // Protocol does not contain a master device, thus contains deployment error and can't be initialized.
        assertFailsWith<IllegalArgumentException>
        {
            Deployment( snapshot, testId )
        }
    }

    @Test
    fun cant_initialize_deployment_with_invalid_snapshot()
    {
        // Initialize valid protocol.
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        val connected = StubMasterDeviceDescriptor( "Connected" )
        protocol.addMasterDevice( master )
        protocol.addConnectedDevice( connected, master )
        val snapshot = protocol.getSnapshot()

        // Create invalid snapshot by editing JSON.
        val json = snapshot.toJson()
        val invalidJson = json.replaceFirst( "\"Master\"", "\"Non-existing device\"" )
        val invalidSnapshot = StudyProtocolSnapshot.fromJson( invalidJson )

        assertFailsWith<IllegalArgumentException>
        {
            Deployment( invalidSnapshot, testId )
        }
    }

    @Test
    fun new_deployment_has_unregistered_master_device()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment: Deployment = deploymentFor( protocol )

        // Two devices can be registered, but none are by default.
        assertEquals( 2, deployment.registrableDevices.size )
        assertTrue { deployment.registrableDevices.map { it.device }.containsAll( protocol.devices ) }
        assertEquals( 0, deployment.registeredDevices.size )

        // Only the master device requires registration.
        val requiredRegistration = deployment.registrableDevices.single { it.requiresRegistration }
        assertEquals( protocol.masterDevices.single(), requiredRegistration.device )
    }

    @Test
    fun registerDevice_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        val deployment: Deployment = deploymentFor( protocol )

        val registration = DeviceRegistration( "0" )
        deployment.registerDevice( device, registration )

        assertEquals( 1, deployment.registeredDevices.size )
        val registered = deployment.registeredDevices.values.single()
        assertEquals( registration, registered )
        assertFalse { registration === registered } // Object should be cloned during registration to prevent modification after registration.
    }

    @Test
    fun cant_registerDevice_not_part_of_deployment()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment: Deployment = deploymentFor( protocol )

        val invalidDevice = StubMasterDeviceDescriptor( "Not part of deployment" )
        val registration = DeviceRegistration( "0" )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( invalidDevice, registration )
        }
    }

    @Test
    fun cant_registerDevice_if_already_registered()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        val deployment: Deployment = deploymentFor( protocol )

        deployment.registerDevice( device, DeviceRegistration( "0" ) )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( device, DeviceRegistration( "1" ))
        }
    }

    /**
     * When the runtime type of devices is unknown, deployment cannot verify whether a registration is valid (this is implemented on the type definition).
     * However, rather than not supporting deployment, registration is simply considered valid and forwarded as is.
     */
    @Test
    fun can_registerDevice_for_unknown_types()
    {
        val protocol = createEmptyProtocol()
        val master = UnknownMasterDeviceDescriptor( "Unknown master" )
        val connected = UnknownDeviceDescriptor( "Unknown connected" )

        // Mimic that the 'Unknown...' types are unknown at runtime. When this occurs, they are wrapped in 'Custom...'.
        val masterCustom = CustomMasterDeviceDescriptor( "Irrelevant", JSON.stringify( master ) )
        val connectedCustom = CustomDeviceDescriptor( "Irrelevant", JSON.stringify( connected ) )

        protocol.addMasterDevice( masterCustom )
        protocol.addConnectedDevice( connectedCustom, masterCustom )

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val deployment = Deployment( snapshot, testId )

        deployment.registerDevice( masterCustom, DeviceRegistration( "0" ) )
        deployment.registerDevice( connectedCustom, DeviceRegistration( "1" ) )
    }
}