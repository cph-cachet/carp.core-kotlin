package dk.cachet.carp.protocols.domain


import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl.Control
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.configuration.ParticipantDataConfiguration
import dk.cachet.carp.protocols.domain.configuration.ParticipantDataConfigurationTest
import dk.cachet.carp.protocols.domain.configuration.ProtocolDeviceConfiguration
import dk.cachet.carp.protocols.domain.configuration.ProtocolDeviceConfigurationTest
import dk.cachet.carp.protocols.domain.configuration.ProtocolTaskConfiguration
import dk.cachet.carp.protocols.domain.configuration.ProtocolTaskConfigurationTest
import dk.cachet.carp.protocols.domain.deployment.NoPrimaryDeviceError
import dk.cachet.carp.protocols.domain.deployment.UnstartedTasksWarning
import dk.cachet.carp.protocols.domain.deployment.UnusedDevicesWarning
import dk.cachet.carp.protocols.domain.deployment.UseCompositeTaskWarning
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
    inner class Devices : ProtocolDeviceConfigurationTest
    {
        override fun createDeviceConfiguration(): ProtocolDeviceConfiguration = createEmptyProtocol()
    }

    @Nested
    inner class Tasks : ProtocolTaskConfigurationTest
    {
        override fun createTaskConfiguration(): ProtocolTaskConfiguration = createEmptyProtocol()
    }

    @Nested
    inner class ParticipantData : ParticipantDataConfigurationTest
    {
        override fun createParticipantDataConfiguration(): ParticipantDataConfiguration = createEmptyProtocol()


        @Test
        fun replaceExpectedParticipantData_succeeds()
        {
            val protocol: StudyProtocol = createEmptyProtocol()
            val attribute1 = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type1" ) )
            val attribute2 = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type2" ) )
            protocol.addExpectedParticipantData( attribute1 )

            val isReplaced = protocol.replaceExpectedParticipantData( setOf( attribute2 ) )

            assertTrue( isReplaced )
            assertEquals( setOf( attribute2 ), protocol.expectedParticipantData )
        }

        @Test
        fun replaceExpectedParticipantData_returns_false_when_nothing_replaced()
        {
            val protocol: StudyProtocol = createEmptyProtocol()
            val attribute1 = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type1" ) )

            protocol.addExpectedParticipantData( attribute1 )
            val isReplaced = protocol.replaceExpectedParticipantData( setOf( attribute1 ) )

            assertFalse( isReplaced )
            assertEquals( setOf( attribute1 ), protocol.expectedParticipantData )
        }

        @Test
        fun replaceExpectedParticipantData_only_triggers_events_for_changes()
        {
            val protocol: StudyProtocol = createEmptyProtocol()
            val attribute1 = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "type1" ) )
            val attribute2 = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "type2" ) )
            protocol.addExpectedParticipantData( attribute1 )
            protocol.addExpectedParticipantData( attribute2 )
            protocol.consumeEvents()

            val attribute3 = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "type3" ) )
            val isReplaced = protocol.replaceExpectedParticipantData( setOf( attribute2, attribute3 ) )

            // Attribute 1 was removed, attribute 3 added, and attribute 2 remained.
            assertTrue( isReplaced )
            val events = protocol.consumeEvents()
            assertEquals( 2, events.size )
            assertEquals(
                StudyProtocol.Event.ExpectedParticipantDataRemoved( attribute1 ),
                events.filterIsInstance<StudyProtocol.Event.ExpectedParticipantDataRemoved>().singleOrNull()
            )
            assertEquals(
                StudyProtocol.Event.ExpectedParticipantDataAdded( attribute3 ),
                events.filterIsInstance<StudyProtocol.Event.ExpectedParticipantDataAdded>().singleOrNull()
            )
        }
    }


    @Test
    fun name_and_description_change_succeeds()
    {
        val protocol = StudyProtocol( UUID.randomUUID(), "Initial name" )

        val newName = "New name"
        val newDescription = "New description"
        protocol.name = newName
        protocol.description = newDescription
        assertEquals( newName, protocol.name )
        assertEquals( newDescription, protocol.description )
        val events = protocol.consumeEvents()
        assertEquals(
            StudyProtocol.Event.NameChanged( newName ),
            events.filterIsInstance<StudyProtocol.Event.NameChanged>().single()
        )
        assertEquals(
            StudyProtocol.Event.DescriptionChanged( newDescription ),
            events.filterIsInstance<StudyProtocol.Event.DescriptionChanged>().single()
        )
    }

    @Test
    fun name_and_description_change_triggers_no_event_for_same_values()
    {
        val name = "Name"
        val description = "Description"
        val protocol = StudyProtocol( UUID.randomUUID(), name, description )

        protocol.name = name
        protocol.description = description
        assertTrue( protocol.consumeEvents().isEmpty() )
    }

    @Test
    fun one_primary_device_needed_for_deployment()
    {
        // By default, no primary device is defined in a study protocol.
        val protocol: StudyProtocol = createEmptyProtocol()

        // Therefore, the protocol is not deployable, indicated by an error in deployment issues.
        assertFalse( protocol.isDeployable() )
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<NoPrimaryDeviceError>().count() )
    }

    @Test
    fun addTrigger_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val trigger = StubTriggerConfiguration( device )

        val assignedTrigger: TriggerWithId = protocol.addTrigger( trigger )
        assertTrue( protocol.triggers.contains( assignedTrigger ) )
        assertEquals( StudyProtocol.Event.TriggerAdded( trigger ), protocol.consumeEvents().last() )
    }

    @Test
    fun addTrigger_multiple_times_only_adds_first_time()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val trigger = StubTriggerConfiguration( device )
        val firstAssign: TriggerWithId = protocol.addTrigger( trigger )

        val secondAssign: TriggerWithId = protocol.addTrigger( trigger )
        assertEquals( firstAssign, secondAssign )
        assertEquals( 1, protocol.triggers.count() )
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( 1, triggerEvents.count() )
    }

    @Test
    fun cant_addTrigger_for_device_not_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val trigger = StubTriggerConfiguration( StubDeviceConfiguration() )

        assertFailsWith<IllegalArgumentException>
        {
            protocol.addTrigger( trigger )
        }
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( 0, triggerEvents.count() )
    }

    @Test
    fun cant_addTrigger_which_requires_a_primary_device_for_a_normal_device()
    {
        val protocol = createEmptyProtocol()
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val connectedDevice = StubDeviceConfiguration()
        protocol.addPrimaryDevice( primaryDevice )
        protocol.addConnectedDevice( connectedDevice, primaryDevice )
        val trigger = StubTriggerConfiguration( connectedDevice.roleName, "Unique", true )

        assertFailsWith<IllegalArgumentException>
        {
            protocol.addTrigger( trigger )
        }
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( 0, triggerEvents.count() )
    }

    @Test
    fun addTaskControl_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val trigger = StubTriggerConfiguration( device )
        val task = StubTaskConfiguration()
        protocol.addPrimaryDevice( device )
        protocol.addTrigger( trigger )

        val isAdded: Boolean = protocol.addTaskControl( trigger, task, device, Control.Start )
        assertTrue( isAdded )
        val control = trigger.start( task, device )
        assertTrue( control in protocol.getTaskControls( trigger ) )
        assertEquals( StudyProtocol.Event.TaskControlAdded( control ), protocol.consumeEvents().last() )
    }

    @Test
    fun addTaskControl_multiple_times_only_adds_first_time()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val trigger = StubTriggerConfiguration( device )
        val task = StubTaskConfiguration()
        protocol.addPrimaryDevice( device )
        protocol.addTaskControl( trigger.start( task, device ) )

        val isAdded = protocol.addTaskControl( trigger.start( task, device ) )
        assertFalse( isAdded )
        assertEquals( 1, protocol.getTaskControls( trigger ).count() )
        val triggeredTaskEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TaskControlAdded>()
        assertEquals( 1, triggeredTaskEvents.count() )
    }

    @Test
    fun addTaskControl_adds_triggers_which_are_not_yet_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val task = StubTaskConfiguration()
        protocol.addPrimaryDevice( device )
        protocol.addTask( task )

        val trigger = StubTriggerConfiguration( device )
        protocol.addTaskControl( trigger, task, device, Control.Start )
        assertTrue( protocol.triggers.any { it.trigger == trigger } )
        val triggerEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TriggerAdded>()
        assertEquals( StudyProtocol.Event.TriggerAdded( trigger ), triggerEvents.single() )
    }

    @Test
    fun addTaskControl_adds_tasks_which_are_not_yet_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val trigger = StubTriggerConfiguration( device )
        protocol.addPrimaryDevice( device )
        protocol.addTrigger( trigger )

        val task = StubTaskConfiguration()
        protocol.addTaskControl( trigger, task, device, Control.Start )
        assertTrue( protocol.tasks.contains( task ) )
        val taskEvents = protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TaskAdded>()
        assertEquals( StudyProtocol.Event.TaskAdded( task ), taskEvents.single() )
    }

    @Test
    fun cant_addTaskControl_for_device_not_included_in_the_protocol()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val trigger = StubTriggerConfiguration( device )
        val task = StubTaskConfiguration()
        with ( protocol )
        {
            addPrimaryDevice( device )
            addTrigger( trigger )
            addTask( task )
        }

        assertFailsWith<IllegalArgumentException>
        {
            protocol.addTaskControl( trigger.start( task, StubDeviceConfiguration() ) )
        }
        assertEquals( 0, protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TaskControlAdded>().count() )
    }

    @Test
    fun getTaskControls_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val trigger = StubTriggerConfiguration( device )
        val otherTrigger = StubTriggerConfiguration( device, "Different" )
        val task = StubTaskConfiguration( "Task one" )
        with ( protocol )
        {
            addPrimaryDevice( device )
            addTaskControl( trigger.start( task, device ) )
            addTaskControl( otherTrigger.start( StubTaskConfiguration( "Task two" ), device ) )
        }

        val taskControls: List<TaskControl> = protocol.getTaskControls( trigger ).toList()
        assertEquals( 1, taskControls.count() )
        assertTrue( TaskControl( trigger, task, device, Control.Start ) in taskControls )
    }

    @Test
    fun cant_getTaskControls_for_nonexisting_trigger()
    {
        val protocol = createEmptyProtocol()

        assertFailsWith<IllegalArgumentException>
        {
            protocol.getTaskControls( StubTriggerConfiguration( StubDeviceConfiguration() ) )
        }
    }

    @Test
    fun getTasksForDevice_succeeds()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration()
        val connected = StubDeviceConfiguration()
        protocol.addPrimaryDevice( primary )
        protocol.addConnectedDevice( connected, primary )
        val priaryTask = StubTaskConfiguration( "Primary task" )
        val connectedTask = StubTaskConfiguration( "Connected task" )
        protocol.addTaskControl( StubTriggerConfiguration( primary ).start( priaryTask, primary ) )
        protocol.addTaskControl( StubTriggerConfiguration( primary ).start( connectedTask, connected ) )

        assertEquals( setOf( priaryTask ), protocol.getTasksForDevice( primary ) )
        assertEquals( setOf( connectedTask ), protocol.getTasksForDevice( connected ) )
    }

    @Test
    fun deployment_warning_when_a_trigger_sends_more_than_one_task_to_a_single_device()
    {
        // Create a study protocol with a trigger which triggers two tasks to a single device.
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val trigger = StubTriggerConfiguration( device )
        with ( protocol ) {
            addPrimaryDevice( device )
            addTaskControl( trigger, StubTaskConfiguration( "Task 1" ), device, Control.Start )
            addTaskControl( trigger, StubTaskConfiguration( "Task 2" ), device, Control.Start )
        }

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UseCompositeTaskWarning>().count() )
    }

    @Test
    fun deployment_warning_when_a_device_is_never_used_in_a_trigger_or_never_relays_data_from_connected_devices()
    {
        // Create a study protocol with a device which is never used.
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val unusedDevice = StubDeviceConfiguration()
        with ( protocol )
        {
            addPrimaryDevice( device )
            addConnectedDevice( unusedDevice, device )
            addTaskControl( StubTriggerConfiguration( device ), StubTaskConfiguration(), device, Control.Start )
        }

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UnusedDevicesWarning>().count() )
    }

    @Test
    fun removeTask_also_removes_it_from_triggers()
    {
        // Create a study protocol with a task which is initiated by a trigger.
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val task = StubTaskConfiguration()
        val trigger1 = StubTriggerConfiguration( device, "Trigger one" )
        val trigger2 = StubTriggerConfiguration( device, "Trigger two" )
        with ( protocol )
        {
            addPrimaryDevice( device )
            addTaskControl( trigger1.start( task, device ) )
            addTaskControl( trigger2.stop( task, device ) )
        }

        protocol.removeTask( task )
        assertEquals( 0, protocol.getTaskControls( trigger1 ).count() )
        assertEquals( 0, protocol.getTaskControls( trigger2 ).count() )
        assertEquals( 2, protocol.consumeEvents().filterIsInstance<StudyProtocol.Event.TaskControlRemoved>().count() )
    }

    @Test
    fun deployment_warning_when_some_tasks_are_never_triggered()
    {
        // Create a study protocol with a task which is never triggered.
        val protocol = createEmptyProtocol()
        protocol.addTask( StubTaskConfiguration() )

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UnstartedTasksWarning>().count() )
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

        assertEquals( protocol.id, fromSnapshot.id )
        assertEquals( protocol.ownerId, fromSnapshot.ownerId )
        assertEquals( protocol.name, fromSnapshot.name )
        assertEquals( protocol.description, fromSnapshot.description )
        assertEquals( protocol.createdOn, fromSnapshot.createdOn )
        assertEquals( protocol.devices, fromSnapshot.devices )
        protocol.primaryDevices.forEach { assertTrue( connectedDevicesAreSame( protocol, fromSnapshot, it ) ) }
        assertEquals( protocol.triggers, fromSnapshot.triggers )
        assertEquals( protocol.tasks, fromSnapshot.tasks )
        protocol.triggers.forEach {
            val triggeredTasks = protocol.getTaskControls( it.id ).toSet()
            val fromSnapshotTriggeredTasks = fromSnapshot.getTaskControls( it.id ).toSet()
            assertEquals( triggeredTasks, fromSnapshotTriggeredTasks )
        }
        assertEquals( protocol.expectedParticipantData, fromSnapshot.expectedParticipantData )
        assertEquals( protocol.applicationData, fromSnapshot.applicationData )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }


    private fun connectedDevicesAreSame(
        protocol: StudyProtocol,
        fromSnapshot: StudyProtocol,
        primaryDevice: AnyPrimaryDeviceConfiguration
    ): Boolean
    {
        val protocolConnected = protocol.getConnectedDevices( primaryDevice ).toSet()
        val snapshotConnected = fromSnapshot.getConnectedDevices( primaryDevice ).toSet()

        val areSameDevices = protocolConnected == snapshotConnected
        return areSameDevices && protocolConnected.filterIsInstance<AnyPrimaryDeviceConfiguration>().all {
            connectedDevicesAreSame( protocol, fromSnapshot, it )
        }
    }
}
