package dk.cachet.carp.protocols.domain

import dk.cachet.carp.test.Nested
import dk.cachet.carp.protocols.domain.deployment.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlin.test.*


/**
 * Tests for [StudyProtocol].
 */
class StudyProtocolTest
{
    @Nested
    inner class Devices : DeviceConfigurationTest
    {
        override fun createDeviceConfiguration(): DeviceConfiguration
        {
            return createEmptyProtocol()
        }
    }

    @Nested
    inner class Tasks : TaskConfigurationTest
    {
        override fun createTaskConfiguration(): TaskConfiguration
        {
            return createEmptyProtocol()
        }
    }


    @Test
    fun one_master_device_needed_for_deployment()
    {
        // By default, no master device is defined in a study protocol.
        val protocol: StudyProtocol = createEmptyProtocol()

        // Therefore, the protocol is not deployable, indicated by an error in deployment issues.
        assertFalse( protocol.isDeployable() )
        assertEquals( 1, protocol.getDeploymentIssues().filter { it is NoMasterDeviceError }.count() )
    }


    @Test
    fun addTrigger_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        val trigger = StubTrigger( device )

        val isAdded: Boolean = protocol.addTrigger( trigger )
        assertTrue( isAdded )
        assertTrue( protocol.triggers.contains( trigger ) )
    }

    @Test
    fun addTrigger_multiple_times_only_adds_first_time()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        val trigger = StubTrigger( device )
        protocol.addTrigger( trigger )

        val isAdded: Boolean = protocol.addTrigger( trigger )
        assertFalse( isAdded )
        assertEquals( 1, protocol.triggers.count() )
    }

    @Test
    fun cant_addTrigger_for_device_not_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val trigger = StubTrigger( StubDeviceDescriptor() )

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.addTrigger( trigger )
        }
    }

    @Test
    fun cant_addTrigger_which_requires_a_master_device_for_a_normal_device()
    {
        val protocol = createEmptyProtocol()
        val masterDevice = StubMasterDeviceDescriptor()
        val connectedDevice = StubDeviceDescriptor()
        protocol.addMasterDevice( masterDevice )
        protocol.addConnectedDevice( connectedDevice, masterDevice )
        val trigger = StubTrigger( connectedDevice.roleName, "Unique", true )

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.addTrigger( trigger )
        }
    }

    @Test
    fun addTriggeredTask_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTrigger( trigger )

        val isAdded: Boolean = protocol.addTriggeredTask( trigger, task, device )
        assertTrue( isAdded )
        assertTrue( protocol.getTriggeredTasks( trigger ).contains( TriggeredTask( task, device ) ) )
    }

    @Test
    fun addTriggeredTasks_multiple_times_only_adds_first_time()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTriggeredTask( trigger, task, device )

        val isAdded = protocol.addTriggeredTask( trigger, task, device )
        assertFalse( isAdded )
        assertEquals( 1, protocol.getTriggeredTasks( trigger ).count() )
    }

    @Test
    fun addTriggeredTask_adds_triggers_which_are_not_yet_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( task )

        val trigger = StubTrigger( device )
        protocol.addTriggeredTask( trigger, task, device )
        assertTrue( protocol.triggers.contains( trigger ) )
    }

    @Test
    fun addTriggeredTask_adds_tasks_which_are_not_yet_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        protocol.addMasterDevice( device )
        protocol.addTrigger( trigger )

        val task = StubTaskDescriptor()
        protocol.addTriggeredTask( trigger, task, device )
        assertTrue( protocol.tasks.contains( task ) )
    }

    @Test
    fun cant_addTriggeredTask_for_device_not_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task = StubTaskDescriptor()
        with ( protocol )
        {
            addMasterDevice( device )
            addTrigger( trigger )
            addTask( task )
        }

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.addTriggeredTask( trigger, task, StubDeviceDescriptor() )
        }
    }

    @Test
    fun getTriggeredTasks_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val otherTrigger = StubTrigger( device, "Different" )
        val task = StubTaskDescriptor( "Task one" )
        with ( protocol )
        {
            addMasterDevice( device )
            addTriggeredTask( trigger, task, device )
            addTriggeredTask( otherTrigger, StubTaskDescriptor( "Task two" ), device )
        }

        val triggeredTasks: List<TriggeredTask> = protocol.getTriggeredTasks( trigger ).toList()
        assertEquals( 1, triggeredTasks.count() )
        assertTrue( triggeredTasks.contains( TriggeredTask( task, device ) ) )
    }

    @Test
    fun cant_getTriggeredTasks_for_nonexisting_trigger()
    {
        val protocol = createEmptyProtocol()

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.getTriggeredTasks( StubTrigger( StubDeviceDescriptor() ) )
        }
    }

    @Test
    fun getTasksForDevice_succeeds()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        val connected = StubDeviceDescriptor()
        protocol.addMasterDevice( master )
        protocol.addConnectedDevice( connected, master )
        val masterTask = StubTaskDescriptor( "Master task" )
        val connectedTask = StubTaskDescriptor( "Connected task" )
        protocol.addTriggeredTask( StubTrigger( master ), masterTask, master )
        protocol.addTriggeredTask( StubTrigger( master ), connectedTask, connected )

        assertEquals( setOf( masterTask ), protocol.getTasksForDevice( master ) )
        assertEquals( setOf( connectedTask ), protocol.getTasksForDevice( connected ) )
    }

    @Test
    fun deployment_warning_when_a_trigger_sends_more_than_one_task_to_a_single_device()
    {
        // Create a study protocol with a trigger which triggers two tasks to a single device.
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        with ( protocol ) {
            addMasterDevice( device )
            addTriggeredTask( trigger, StubTaskDescriptor( "Task 1" ), device )
            addTriggeredTask( trigger, StubTaskDescriptor( "Task 2" ), device )
        }

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UseCompositeTaskWarning>().count() )
    }

    @Test
    fun deployment_warning_when_a_device_is_never_used_in_a_trigger_or_never_relays_data_from_connected_devices()
    {
        // Create a study protocol with a device which is never used.
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val unusedDevice = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( device )
            addConnectedDevice( unusedDevice, device )
            addTriggeredTask( StubTrigger( device ), StubTaskDescriptor(), device )
        }

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UnusedDevicesWarning>().count() )
    }

    @Test
    fun removeTask_also_removes_it_from_triggers()
    {
        // Create a study protocol with a task which is initiated by a trigger.
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val task = StubTaskDescriptor()
        val trigger1 = StubTrigger( device, "Trigger one" )
        val trigger2 = StubTrigger( device, "Trigger two" )
        with ( protocol )
        {
            addMasterDevice( device )
            addTriggeredTask( trigger1, task, device )
            addTriggeredTask( trigger2, task, device )
        }

        protocol.removeTask( task )
        assertEquals( 0, protocol.getTriggeredTasks( trigger1 ).count() )
        assertEquals( 0, protocol.getTriggeredTasks( trigger2 ).count() )
    }

    @Test
    fun deployment_warning_when_some_tasks_are_never_triggered()
    {
        // Create a study protocol with a task which is never triggered.
        val protocol = createEmptyProtocol()
        protocol.addTask( StubTaskDescriptor() )

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UntriggeredTasksWarning>().count() )
    }

    @Test
    fun creating_protocol_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val protocol = createComplexProtocol()

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val fromSnapshot = StudyProtocol.fromSnapshot( snapshot )

        assertEquals( protocol.owner, fromSnapshot.owner )
        assertEquals( protocol.name, fromSnapshot.name )
        assertEquals( protocol.devices.count(), protocol.devices.intersect( fromSnapshot.devices ).count() )
        protocol.masterDevices.forEach { assertTrue( connectedDevicesAreSame( protocol, fromSnapshot, it ) ) }
        assertEquals( protocol.triggers.count(), protocol.triggers.intersect( fromSnapshot.triggers ).count() )
        assertEquals( protocol.tasks.count(), protocol.tasks.intersect( fromSnapshot.tasks ).count() )
        protocol.triggers.forEach {
            val triggeredTasks = protocol.getTriggeredTasks( it )
            val fromSnapshotTriggeredTasks = fromSnapshot.getTriggeredTasks( it )
            assertEquals( triggeredTasks.count(), triggeredTasks.intersect( fromSnapshotTriggeredTasks ).count() )
        }
    }

    private fun connectedDevicesAreSame( protocol: StudyProtocol, fromSnapshot: StudyProtocol, masterDevice: MasterDeviceDescriptor<*> ): Boolean
    {
        val protocolConnected = protocol.getConnectedDevices( masterDevice ).sortedWith( compareBy { it.roleName } )
        val snapshotConnected = fromSnapshot.getConnectedDevices( masterDevice ).sortedWith( compareBy { it.roleName } )

        val areSameDevices = snapshotConnected.count() == protocolConnected.intersect( snapshotConnected ).count()
        return areSameDevices && protocolConnected.filterIsInstance<MasterDeviceDescriptor<*>>().all { connectedDevicesAreSame( protocol, fromSnapshot, it ) }
    }
}