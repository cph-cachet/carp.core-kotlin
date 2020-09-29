package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
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
            device,
            registration,
            setOf( connected ),
            mapOf( connected.roleName to connectedRegistration ),
            setOf( task ),
            mapOf( 0 to masterTrigger, 1 to connectedTrigger ),
            setOf(
                MasterDeviceDeployment.TriggeredTask( 0, task.name, device.roleName ),
                MasterDeviceDeployment.TriggeredTask( 1, task.name, connected.roleName )
            )
        )
        val tasks: Map<AnyDeviceDescriptor, Set<TaskDescriptor>> = deployment.getTasksPerDevice()

        assertEquals( 2, tasks.size )
        assertEquals( task, tasks[ device ]?.single() )
        assertEquals( task, tasks[ connected ]?.single() )
    }

    @Test
    fun getTaskPerDevice_with_other_master_device_target_succeeds()
    {
        val master1 = StubMasterDeviceDescriptor( "Master 1" )
        val master2 = StubMasterDeviceDescriptor( "Master 2" )
        val master1Registration = master1.createRegistration()
        val master1Trigger = StubTrigger( master1.roleName )

        val deployment = MasterDeviceDeployment(
            master1,
            master1Registration,
            emptySet(),
            emptyMap(),
            emptySet(),
            mapOf( 0 to master1Trigger ),
            setOf( MasterDeviceDeployment.TriggeredTask( 0, "Task on Master 2", master2.roleName ) )
        )
        val tasks: Map<AnyDeviceDescriptor, Set<TaskDescriptor>> = deployment.getTasksPerDevice()

        assertTrue( tasks.isEmpty() )
    }
}
