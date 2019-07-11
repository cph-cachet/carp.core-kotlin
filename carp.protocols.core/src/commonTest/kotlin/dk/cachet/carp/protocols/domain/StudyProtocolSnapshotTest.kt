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
    fun order_of_elements_in_snapshot_does_not_matter_for_equality_or_hashcode()
    {
        val masterDevices = listOf<MasterDeviceDescriptor<*>>( StubMasterDeviceDescriptor( "M1" ), StubMasterDeviceDescriptor( "M2" ) )
        val connectedDevices = listOf<DeviceDescriptor<*>>( StubDeviceDescriptor( "C1" ), StubDeviceDescriptor( "C2" ) )
        val connections = listOf(
            StudyProtocolSnapshot.DeviceConnection( "C1", "M1" ),
            StudyProtocolSnapshot.DeviceConnection( "C2", "M2" ) )
        val tasks = listOf<TaskDescriptor>( StubTaskDescriptor( "T1" ), StubTaskDescriptor( "T2" ) )
        val triggers = listOf(
            StudyProtocolSnapshot.TriggerWithId( 0, StubTrigger( masterDevices[ 0 ] ) ),
            StudyProtocolSnapshot.TriggerWithId( 1, StubTrigger( masterDevices[ 1 ] ) ) )
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
            tasks.reversed(), triggers.reversed(), triggeredTasks.reversed() )

        assertEquals( snapshot, reorganizedSnapshot )
        assertEquals( snapshot.hashCode(), reorganizedSnapshot.hashCode() )
    }
}