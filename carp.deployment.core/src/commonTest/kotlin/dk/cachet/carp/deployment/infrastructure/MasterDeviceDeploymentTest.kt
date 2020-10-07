package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTrigger
import kotlin.test.*


/**
 * Tests for [MasterDeviceDeployment] relying on core infrastructure.
 */
class MasterDeviceDeploymentTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createDeploymentSerializer( STUBS_SERIAL_MODULE )
    }

    @Test
    fun can_serialize_and_deserialize_devicedeployment_using_JSON()
    {
        val device = StubMasterDeviceDescriptor()
        val masterRegistration = device.createRegistration()
        val connected = StubDeviceDescriptor( "Connected" )
        val connectedRegistration = connected.createRegistration()
        val task = StubTaskDescriptor( "Task" )
        val trigger = StubTrigger( connected.roleName )

        val deployment = MasterDeviceDeployment(
            StubMasterDeviceDescriptor(),
            masterRegistration,
            setOf( connected ),
            mapOf( connected.roleName to connectedRegistration ),
            setOf( task ),
            mapOf( 0 to trigger ),
            setOf( MasterDeviceDeployment.TriggeredTask( 0, task.name, connected.roleName ) )
        )

        val json = deployment.toJson()
        val parsed = MasterDeviceDeployment.fromJson( json )
        assertEquals( deployment, parsed )
    }

    @Test
    fun getTasksPerDevice_succeeds()
    {
        val device = StubMasterDeviceDescriptor( "Master" )
        val registration = device.createRegistration()
        val connected = StubDeviceDescriptor( "Connected" )
        val connectedRegistration = connected.createRegistration()
        val task = StubTaskDescriptor()
        val masterTrigger = StubTrigger( device.roleName )
        val connectedTrigger = StubTrigger( connected.roleName )

        val deployment = MasterDeviceDeployment(
            deviceDescriptor = device,
            configuration = registration,
            connectedDevices = setOf( connected ),
            connectedDeviceConfigurations = mapOf( connected.roleName to connectedRegistration ),
            tasks = setOf( task ),
            triggers = mapOf( 0 to masterTrigger, 1 to connectedTrigger ),
            triggeredTasks = setOf(
                MasterDeviceDeployment.TriggeredTask( 0, task.name, device.roleName ),
                MasterDeviceDeployment.TriggeredTask( 1, task.name, connected.roleName )
            )
        )
        val deviceTasks: List<MasterDeviceDeployment.DeviceTasks> = deployment.getTasksPerDevice()

        assertEquals( 2, deviceTasks.size )
        assertEquals( task, deviceTasks.first { it.device == device }.tasks.single() )
        assertEquals( task, deviceTasks.first {it.device == connected }.tasks.single() )
    }

    @Test
    fun getTaskPerDevice_with_other_master_device_target_succeeds()
    {
        val master1 = StubMasterDeviceDescriptor( "Master 1" )
        val master2 = StubMasterDeviceDescriptor( "Master 2" )
        val master1Registration = master1.createRegistration()
        val master1Trigger = StubTrigger( master1.roleName )

        val deployment = MasterDeviceDeployment(
            deviceDescriptor = master1,
            configuration = master1Registration,
            connectedDevices = emptySet(),
            connectedDeviceConfigurations = emptyMap(),
            tasks = emptySet(),
            triggers = mapOf( 0 to master1Trigger ),
            triggeredTasks = setOf(
                MasterDeviceDeployment.TriggeredTask( 0, "Task on Master 2", master2.roleName )
            )
        )
        val tasks: List<MasterDeviceDeployment.DeviceTasks> = deployment.getTasksPerDevice()

        assertTrue( tasks.isEmpty() )
    }
}
