package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.triggers.*
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
        protocol.addTriggeredTask( trigger, task, masterDevice )
        val newTriggeredTaskSnapshot = protocol.getSnapshot()
        assertTrue( newTaskSnapshot != newTriggeredTaskSnapshot )
        assertTrue( newTaskSnapshot.hashCode() != newTriggeredTaskSnapshot.hashCode() )
    }

    @Test
    fun order_of_tasks_and_devices_in_snapshot_does_not_matter_for_equality_or_hashcode()
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
            StudyProtocolSnapshot.TriggeredTask( 0, "T1", "C1" ),
            StudyProtocolSnapshot.TriggeredTask( 1, "T2", "C2" )
        )

        val ownerId = UUID( "ef26be3f-2de8-4779-a608-bb6e027e4b75" )
        val snapshot = StudyProtocolSnapshot(
            ownerId, "Study",
            masterDevices, connectedDevices, connections,
            tasks, triggers, triggeredTasks )
        val reorganizedSnapshot = StudyProtocolSnapshot(
            ownerId, "Study",
            masterDevices.reversed(), connectedDevices.reversed(), connections.reversed(),
            tasks.reversed(), triggers, triggeredTasks.reversed() )

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

        val protocol1: StudyProtocolSnapshot = createEmptyProtocol().apply {
            addMasterDevice( device1 )
            addMasterDevice( device2 )
            addTrigger( trigger1 )
            addTrigger( trigger2 )
        }.getSnapshot()

        val protocol2: StudyProtocolSnapshot = createEmptyProtocol().apply {
            addMasterDevice( device1 )
            addMasterDevice( device2 )
            addTrigger( trigger2 )
            addTrigger( trigger1 )
        }.getSnapshot()

        assertTrue( protocol1 == protocol2 )
    }
}
