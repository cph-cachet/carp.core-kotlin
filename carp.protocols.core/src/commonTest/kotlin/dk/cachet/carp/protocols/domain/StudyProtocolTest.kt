package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.protocols.domain.deployment.NoMasterDeviceError
import dk.cachet.carp.protocols.domain.deployment.UntriggeredTasksWarning
import dk.cachet.carp.protocols.domain.deployment.UnusedDevicesWarning
import dk.cachet.carp.protocols.domain.deployment.UseCompositeTaskWarning
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceConfiguration
import dk.cachet.carp.protocols.domain.devices.DeviceConfigurationTest
import dk.cachet.carp.protocols.domain.tasks.TaskConfiguration
import dk.cachet.carp.protocols.domain.tasks.TaskConfigurationTest
import dk.cachet.carp.protocols.domain.triggers.TriggeredTask
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTrigger
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.test.Nested
import kotlin.test.*


/**
 * Tests for [StudyProtocol].
 */
class StudyProtocolTest
{
    @Nested
    inner class Devices : DeviceConfigurationTest
    {
        override fun createDeviceConfiguration(): DeviceConfiguration = createEmptyProtocol()
    }

    @Nested
    inner class Tasks : TaskConfigurationTest
    {
        override fun createTaskConfiguration(): TaskConfiguration = createEmptyProtocol()
    }

    @Nested
    inner class ParticipantData : ParticipantDataConfigurationTest
    {
        override fun createParticipantDataConfiguration(): ParticipantDataConfiguration = createEmptyProtocol()
    }


    @Test
    fun one_master_device_needed_for_deployment()
    {
        // By default, no master device is defined in a study protocol.
        val protocol: StudyProtocol = createEmptyProtocol()

        // Therefore, the protocol is not deployable, indicated by an error in deployment issues.
        assertFalse( protocol.isDeployable() )
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<NoMasterDeviceError>().count() )
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
        assertEquals( StudyProtocol.Event.TriggerAdded( trigger ), protocol.consumeEvents().last() )
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
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( 1, triggerEvents.count() )
    }

    @Test
    fun cant_addTrigger_for_device_not_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val trigger = StubTrigger( StubDeviceDescriptor() )

        assertFailsWith<IllegalArgumentException>
        {
            protocol.addTrigger( trigger )
        }
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( 0, triggerEvents.count() )
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

        assertFailsWith<IllegalArgumentException>
        {
            protocol.addTrigger( trigger )
        }
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( 0, triggerEvents.count() )
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
        assertEquals( StudyProtocol.Event.TriggeredTaskAdded( TriggeredTask( task, device ) ), protocol.consumeEvents().last() )
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
        val triggeredTaskEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggeredTaskAdded>()
        assertEquals( 1, triggeredTaskEvents.count() )
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
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( StudyProtocol.Event.TriggerAdded( trigger ), triggerEvents.single() )
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
        val taskEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TaskAdded>()
        assertEquals( StudyProtocol.Event.TaskAdded( task ), taskEvents.single() )
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

        assertFailsWith<IllegalArgumentException>
        {
            protocol.addTriggeredTask( trigger, task, StubDeviceDescriptor() )
        }
        assertEquals( 0, protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggeredTaskAdded>().count() )
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

        assertFailsWith<IllegalArgumentException>
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
        assertEquals( 2, protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggeredTaskRemoved>().count() )
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
    fun addExpectedParticipantData_succeeds()
    {
        val protocol = createEmptyProtocol()

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val isAdded = protocol.addExpectedParticipantData( attribute )

        assertTrue( isAdded )
        val addedEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.ExpectedParticipantDataAdded>()
        assertEquals( 1, addedEvents.count() )
        assertEquals( attribute, addedEvents.single().attribute )
    }

    @Test
    fun removeExpectedParticipantData_succeeds()
    {
        val protocol = createEmptyProtocol()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        protocol.addExpectedParticipantData( attribute )
        protocol.consumeEvents()

        val isRemoved = protocol.removeExpectedParticipantData( attribute )

        assertTrue( isRemoved )
        val removedEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.ExpectedParticipantDataRemoved>()
        assertEquals( 1, removedEvents.count() )
        assertEquals( attribute, removedEvents.single().attribute )
    }

    @Test
    fun creating_protocol_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val protocol = createComplexProtocol()

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val fromSnapshot = StudyProtocol.fromSnapshot( snapshot )

        assertEquals( protocol.owner, fromSnapshot.owner )
        assertEquals( protocol.name, fromSnapshot.name )
        assertEquals( protocol.description, fromSnapshot.description )
        assertEquals( protocol.creationDate, fromSnapshot.creationDate )
        assertEquals( protocol.devices, fromSnapshot.devices )
        protocol.masterDevices.forEach { assertTrue( connectedDevicesAreSame( protocol, fromSnapshot, it ) ) }
        assertEquals( protocol.triggers, fromSnapshot.triggers )
        assertEquals( protocol.tasks, fromSnapshot.tasks )
        protocol.triggers.forEach {
            val triggeredTasks = protocol.getTriggeredTasks( it )
            val fromSnapshotTriggeredTasks = fromSnapshot.getTriggeredTasks( it )
            assertEquals( triggeredTasks.count(), triggeredTasks.intersect( fromSnapshotTriggeredTasks ).count() )
        }
        assertEquals( protocol.expectedParticipantData, fromSnapshot.expectedParticipantData )
    }

    private fun connectedDevicesAreSame( protocol: StudyProtocol, fromSnapshot: StudyProtocol, masterDevice: AnyMasterDeviceDescriptor ): Boolean
    {
        val protocolConnected = protocol.getConnectedDevices( masterDevice ).sortedWith( compareBy { it.roleName } )
        val snapshotConnected = fromSnapshot.getConnectedDevices( masterDevice ).sortedWith( compareBy { it.roleName } )

        val areSameDevices = snapshotConnected.count() == protocolConnected.intersect( snapshotConnected ).count()
        return areSameDevices && protocolConnected.filterIsInstance<AnyMasterDeviceDescriptor>().all { connectedDevicesAreSame( protocol, fromSnapshot, it ) }
    }
}
