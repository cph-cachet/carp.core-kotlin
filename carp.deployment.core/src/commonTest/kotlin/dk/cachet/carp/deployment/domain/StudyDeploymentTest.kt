package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.protocols.domain.devices.CustomDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.CustomMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [StudyDeployment].
 */
class StudyDeploymentTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun cant_initialize_deployment_with_errors()
    {
        val protocol = createEmptyProtocol()
        val snapshot = protocol.getSnapshot()

        // Protocol does not contain a master device, thus contains deployment error and can't be initialized.
        assertFailsWith<IllegalArgumentException>
        {
            StudyDeployment( snapshot )
        }
    }

    @Test
    fun new_deployment_has_unregistered_master_device()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

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
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val registration = DefaultDeviceRegistration( "0" )
        deployment.registerDevice( device, registration )

        assertEquals( 1, deployment.registeredDevices.size )
        val registered = deployment.registeredDevices.values.single()
        assertEquals( registration, registered )
    }

    @Test
    fun cant_registerDevice_not_part_of_deployment()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val invalidDevice = StubMasterDeviceDescriptor( "Not part of deployment" )
        val registration = DefaultDeviceRegistration( "0" )

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
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        deployment.registerDevice( device, DefaultDeviceRegistration( "0" ) )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( device, DefaultDeviceRegistration( "1" ))
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
        val masterCustom = CustomMasterDeviceDescriptor( "Irrelevant", JSON.stringify( UnknownMasterDeviceDescriptor.serializer(), master ), JSON )
        val connectedCustom = CustomDeviceDescriptor( "Irrelevant", JSON.stringify( UnknownDeviceDescriptor.serializer(), connected ), JSON )

        protocol.addMasterDevice( masterCustom )
        protocol.addConnectedDevice( connectedCustom, masterCustom )

        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( masterCustom, DefaultDeviceRegistration( "0" ) )
        deployment.registerDevice( connectedCustom, DefaultDeviceRegistration( "1" ) )
    }

    @Test
    fun cant_registerDevice_already_in_use_by_different_role()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        val connected = StubMasterDeviceDescriptor( "Connected" )
        protocol.addMasterDevice( master )
        protocol.addConnectedDevice( connected, master )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val registration = DefaultDeviceRegistration( "0" )
        deployment.registerDevice( master, registration )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( connected, registration )
        }
    }

    @Test
    fun can_registerDevice_with_same_id_for_two_different_unknown_types()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        val device1 = UnknownMasterDeviceDescriptor( "Unknown device 1" )
        val device2 = UnknownDeviceDescriptor( "Unknown device 2" )

        // Mimic that the 'Unknown...' types are unknown at runtime. When this occurs, they are wrapped in 'Custom...'.
        val device1Custom = CustomDeviceDescriptor( "One class", JSON.stringify( UnknownMasterDeviceDescriptor.serializer(), device1 ), JSON )
        val device2Custom = CustomDeviceDescriptor( "Not the same class", JSON.stringify( UnknownDeviceDescriptor.serializer(), device2 ), JSON )

        protocol.addMasterDevice( master )
        protocol.addConnectedDevice( device1Custom, master )
        protocol.addConnectedDevice( device2Custom, master )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        // Even though these two devices are registered using the same ID, this should succeed since they are of different types.
        deployment.registerDevice( device1Custom, DefaultDeviceRegistration( "0" ) )
        deployment.registerDevice( device2Custom, DefaultDeviceRegistration( "0" ) )
    }

    @Test
    fun cant_registerDevice_with_wrong_registration_type()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        protocol.addMasterDevice( master )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val wrongRegistration = UnknownDeviceRegistration( "0" )
        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( master, wrongRegistration )
        }
    }

    @Test
    fun creating_deployment_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        val snapshot: StudyDeploymentSnapshot = deployment.getSnapshot()
        val fromSnapshot = StudyDeployment.fromSnapshot( snapshot )

        assertEquals( deployment.id, fromSnapshot.id )
        assertEquals( deployment.protocolSnapshot, fromSnapshot.protocolSnapshot )
        assertEquals(
            deployment.registrableDevices.count(),
            deployment.registrableDevices.intersect( fromSnapshot.registrableDevices ).count() )
        assertEquals(
            deployment.registeredDevices.count(),
            deployment.registeredDevices.entries.intersect( fromSnapshot.registeredDevices.entries ).count() )
    }

    @Test
    fun getStatus_lifecycle_master_and_connected()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol( "Master", "Connected" )
        val master = protocol.devices.first { it.roleName == "Master" }
        val connected = protocol.devices.first { it.roleName == "Connected" }
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        // Start of deployment, no devices registered.
        val status: StudyDeploymentStatus = deployment.getStatus()
        assertEquals( deployment.id, status.studyDeploymentId )
        assertEquals( 2, status.devicesStatus.count() )
        assertTrue { status.devicesStatus.any { it.device == master } }
        assertTrue { status.devicesStatus.any { it.device == connected } }
        assertEquals( setOf( master ), status.getRemainingDevicesToRegister() )
        assertTrue { status.getRemainingDevicesReadyToDeploy().isEmpty() }

        // After registering master device, master device is ready for deployment.
        deployment.registerDevice( master, DefaultDeviceRegistration( "0" ) )
        val readyStatus = deployment.getStatus()
        assertTrue { readyStatus.getRemainingDevicesToRegister().isEmpty() }
        assertEquals( setOf( master ), readyStatus.getRemainingDevicesReadyToDeploy() )
    }

    @Test
    fun chained_master_devices_do_not_require_registration_or_deployment()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        val chained = StubMasterDeviceDescriptor( "Chained master" )
        protocol.addMasterDevice( master )
        protocol.addConnectedDevice( chained, master )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val status: StudyDeploymentStatus = deployment.getStatus()
        val chainedStatus = status.devicesStatus.first { it.device == chained }
        assertFalse { chainedStatus.requiresDeployment }
        assertFalse { chainedStatus.requiresRegistration }
    }

    @Test
    fun getDeviceDeploymentFor_succeeds()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol( "Master", "Connected" )
        val master = protocol.masterDevices.first { it.roleName == "Master" }
        val connected = protocol.devices.first { it.roleName == "Connected" }
        val masterTask = StubTaskDescriptor( "Master task" )
        val connectedTask = StubTaskDescriptor( "Connected task" )
        protocol.addTriggeredTask( master.atStartOfStudy(), masterTask, master )
        protocol.addTriggeredTask( master.atStartOfStudy(), connectedTask, connected )
        val deployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration( "Registered master" )
        deployment.registerDevice( master, registration )

        // Include an additional master device with a trigger which should not impact the `DeviceDeployment` tested here.
        val ignoredMaster = StubMasterDeviceDescriptor( "Ignored" )
        protocol.addMasterDevice( ignoredMaster )
        protocol.addTriggeredTask( ignoredMaster.atStartOfStudy(), masterTask, ignoredMaster )

        val deviceDeployment: MasterDeviceDeployment = deployment.getDeviceDeploymentFor( master )
        assertEquals( "Registered master", deviceDeployment.configuration.deviceId )
        assertEquals( protocol.getConnectedDevices( master ).toSet(), deviceDeployment.connectedDevices )
        assertEquals( 0, deviceDeployment.connectedDeviceConfigurations.count() ) // No preregistered connected devices.

        // Device deployment lists both tasks, even if one is destined for the connected device.
        assertEquals( protocol.tasks.count(), deviceDeployment.tasks.intersect( protocol.tasks ).count() )

        // Device deployment contains correct trigger information.
        assertEquals(1, deviceDeployment.triggers.count() )
        assertEquals( master.atStartOfStudy(), deviceDeployment.triggers[ 0 ] )
        assertEquals(2, deviceDeployment.triggeredTasks.count() )
        assertTrue( deviceDeployment.triggeredTasks.contains( MasterDeviceDeployment.TriggeredTask( 0, masterTask.name, master.roleName ) ) )
        assertTrue( deviceDeployment.triggeredTasks.contains( MasterDeviceDeployment.TriggeredTask( 0, connectedTask.name, connected.roleName ) ) )
    }

    @Test
    fun getDeviceDeploymentFor_with_preregistered_device_succeeds()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol( "Master", "Connected" )
        val master = protocol.masterDevices.first { it.roleName == "Master" }
        val connected = protocol.devices.first { it.roleName == "Connected" }
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( master, DefaultDeviceRegistration( "0" ) )

        deployment.registerDevice( connected, DefaultDeviceRegistration( "42" ) )
        val deviceDeployment = deployment.getDeviceDeploymentFor( master )

        assertEquals( "Connected", deviceDeployment.connectedDeviceConfigurations.keys.single() )
        assertEquals( "42", deviceDeployment.connectedDeviceConfigurations.getValue( "Connected" ).deviceId )
    }

    @Test
    fun getDeviceDeploymentFor_fails_when_device_not_in_protocol()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.getDeviceDeploymentFor( StubMasterDeviceDescriptor( "Some other device" ) )
        }
    }

    @Test
    fun getDeviceDeploymentFor_fails_when_device_cant_be_deployed_yet()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol( "Master", "Connected" )
        val master = protocol.masterDevices.first { it.roleName == "Master" }
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>{ deployment.getDeviceDeploymentFor( master ) }
    }

    @Test
    fun addParticipation_and_retrieving_it_succeeds()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        val account = Account.withUsernameIdentity( "test" )
        val participation = Participation( deployment.id )
        deployment.addParticipation( account, participation )
        val retrievedParticipation = deployment.getParticipation( account )

        assertEquals( participation, retrievedParticipation )
    }

    @Test
    fun addParticipation_for_incorrect_study_deployment_fails()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        val account = Account.withUsernameIdentity( "test" )
        val incorrectDeploymentId = UUID.randomUUID()
        val participation = Participation( incorrectDeploymentId )

        assertFailsWith<IllegalArgumentException> { deployment.addParticipation( account, participation ) }
    }

    @Test
    fun addParticipation_for_existing_account_fails()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        val account = Account.withUsernameIdentity( "test" )
        deployment.addParticipation( account, Participation( deployment.id ) )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.addParticipation( account, Participation( deployment.id ) )
        }
    }

    @Test
    fun getParticipation_for_non_participating_account_returns_null()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        val account = Account.withUsernameIdentity( "test" )

        val participation = deployment.getParticipation( account )
        assertNull( participation )
    }
}
