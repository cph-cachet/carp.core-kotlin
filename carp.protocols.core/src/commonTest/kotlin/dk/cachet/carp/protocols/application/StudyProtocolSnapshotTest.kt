package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


/**
 * Tests for [StudyProtocolSnapshot].
 */
class StudyProtocolSnapshotTest
{
    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun equals_and_hashcode_when_comparing_snapshots_of_same_protocol()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        val snapshot1: StudyProtocolSnapshot = protocol.getSnapshot()
        val snapshot2: StudyProtocolSnapshot = protocol.getSnapshot()

        assertTrue( snapshot1 == snapshot2 )
        assertTrue( snapshot1.hashCode() == snapshot2.hashCode() )
    }

    @Test
    fun equals_and_hashcode_when_comparing_snapshots_of_modified_protocol()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        val original: StudyProtocolSnapshot = protocol.getSnapshot()

        // New master device.
        val masterDevice = StubMasterDeviceDescriptor( "New master" )
        protocol.addMasterDevice( masterDevice )
        val newMasterDeviceSnapshot = protocol.getSnapshot()
        assertTrue( original != newMasterDeviceSnapshot )
        assertTrue( original.hashCode() != newMasterDeviceSnapshot.hashCode() )

        // New connected device.
        val connectedDevice = StubDeviceDescriptor( "New connected" )
        protocol.addConnectedDevice( connectedDevice, masterDevice )
        val newConnectedDeviceSnapshot = protocol.getSnapshot()
        assertTrue( newMasterDeviceSnapshot != newConnectedDeviceSnapshot )
        assertTrue( newMasterDeviceSnapshot.hashCode() != newConnectedDeviceSnapshot.hashCode() )

        // New trigger.
        val trigger = StubTrigger( masterDevice )
        protocol.addTrigger( trigger )
        val newTriggerSnapshot = protocol.getSnapshot()
        assertTrue( newConnectedDeviceSnapshot != newTriggerSnapshot )
        assertTrue( newConnectedDeviceSnapshot.hashCode() != newTriggerSnapshot.hashCode() )

        // New task.
        val task = StubTaskDescriptor( "New task" )
        protocol.addTask( task )
        val newTaskSnapshot = protocol.getSnapshot()
        assertTrue( newTriggerSnapshot != newTaskSnapshot )
        assertTrue( newTriggerSnapshot.hashCode() != newTaskSnapshot.hashCode() )

        // New triggered task.
        protocol.addTaskControl( trigger, task, masterDevice, TaskControl.Control.Start )
        val newTriggeredTaskSnapshot = protocol.getSnapshot()
        assertTrue( newTaskSnapshot != newTriggeredTaskSnapshot )
        assertTrue( newTaskSnapshot.hashCode() != newTriggeredTaskSnapshot.hashCode() )
    }

    @Test
    fun order_of_tasks_devices_and_expected_participant_data_in_snapshot_does_not_matter_for_equality_or_hashcode()
    {
        val masterDevices = listOf<AnyMasterDeviceDescriptor>( StubMasterDeviceDescriptor( "M1" ), StubMasterDeviceDescriptor( "M2" ) )
        val connectedDevices = listOf<AnyDeviceDescriptor>( StubDeviceDescriptor( "C1" ), StubDeviceDescriptor( "C2" ) )
        val connections = listOf(
            StudyProtocolSnapshot.DeviceConnection( "C1", "M1" ),
            StudyProtocolSnapshot.DeviceConnection( "C2", "M2" ) )
        val tasks = listOf<TaskDescriptor>( StubTaskDescriptor( "T1" ), StubTaskDescriptor( "T2" ) )
        val triggers = mapOf<Int, Trigger>(
            0 to StubTrigger( masterDevices[ 0 ] ),
            1 to StubTrigger( masterDevices[ 1 ] ) )
        val triggeredTasks = listOf(
            TaskControl( 0, "T1", "C1", TaskControl.Control.Start ),
            TaskControl( 1, "T2", "C2", TaskControl.Control.Start )
        )
        val expectedParticipantData = listOf(
            ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) ),
            ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "othertype" ) ),
        )

        val ownerId = UUID( "ef26be3f-2de8-4779-a608-bb6e027e4b75" )
        val creationDate = DateTime.now()
        val protocolId = StudyProtocolId( ownerId, "Study" )
        val snapshot = StudyProtocolSnapshot(
            protocolId,
            "Description",
            creationDate,
            masterDevices, connectedDevices, connections,
            tasks, triggers, triggeredTasks, expectedParticipantData )
        val reorganizedSnapshot = StudyProtocolSnapshot(
            protocolId,
            "Description",
            creationDate,
            masterDevices.reversed(), connectedDevices.reversed(), connections.reversed(),
            tasks.reversed(), triggers, triggeredTasks.reversed(), expectedParticipantData.reversed() )

        assertEquals( snapshot, reorganizedSnapshot )
        assertEquals( snapshot.hashCode(), reorganizedSnapshot.hashCode() )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun order_of_triggers_does_not_matter_for_snapshot_equality()
    {
        val device1 = StubMasterDeviceDescriptor( "One" )
        val device2 = StubMasterDeviceDescriptor( "Two" )
        val trigger1 = StubTrigger( "One" )
        val trigger2 = StubTrigger( "Two" )

        // Create two identical base protocols. protocol1 is cloned to make sure `creationDate` is the same.
        val protocol1 = createEmptyProtocol()
        val protocol2 = StudyProtocol.fromSnapshot( protocol1.getSnapshot() )

        val snapshot1: StudyProtocolSnapshot = protocol1.apply {
            addMasterDevice( device1 )
            addMasterDevice( device2 )
            addTrigger( trigger1 )
            addTrigger( trigger2 )
        }.getSnapshot()

        val snapshot2: StudyProtocolSnapshot = protocol2.apply {
            addMasterDevice( device1 )
            addMasterDevice( device2 )
            addTrigger( trigger2 )
            addTrigger( trigger1 )
        }.getSnapshot()

        assertTrue( snapshot1 == snapshot2 )
    }
}
