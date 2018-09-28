package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.data.CustomDataType
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.CustomMeasure
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
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()

        val serialized: String = snapshot.toJson()
        val parsed: StudyProtocolSnapshot = StudyProtocolSnapshot.fromJson( serialized )

        assertEquals( snapshot, parsed )
    }

    /**
     * Types not known at compile time should not prevent deserializing a protocol, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    @JsIgnore
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()

        val parsed = StudyProtocolSnapshot.fromJson( serialized )
        assertEquals( 1, parsed.masterDevices.filter { m -> m is CustomMasterDeviceDescriptor }.count() )
        assertEquals( 1, parsed.connectedDevices.filter { m -> m is CustomDeviceDescriptor }.count() )
        assertEquals( 1, parsed.tasks.filter { m -> m is CustomTaskDescriptor }.count() )
        val allMeasures = parsed.tasks.flatMap{ t -> t.measures }
        assertEquals( 2, allMeasures.filter { m -> m is CustomMeasure }.count() )
        assertEquals( 1, allMeasures.map { m -> m.type }.filter { t -> t is CustomDataType }.count() )
        assertEquals( 1, parsed.triggers.filter { t -> t.trigger is CustomTrigger }.count() )
    }

    @Test
    @JsIgnore
    fun unknown_connected_master_device_is_deserialized_as_a_master_device()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        protocol.addMasterDevice( master )
        val unknownMaster = UnknownMasterDeviceDescriptor( "Unknown master" )
        protocol.addConnectedDevice( unknownMaster, master )

        var serialized = protocol.getSnapshot().toJson()
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownMasterDeviceDescriptor", "com.unknown.CustomMasterDevice" )

        val parsed = StudyProtocolSnapshot.fromJson( serialized )
        assertTrue { parsed.connectedDevices.single() is MasterDeviceDescriptor }
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    @JsIgnore
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot = StudyProtocolSnapshot.fromJson( serialized )

        val customSerialized = snapshot.toJson()
        assertEquals( serialized, customSerialized )
    }

    @Test
    fun order_of_elements_in_snapshot_does_not_matter_for_equality_or_hashcode()
    {
        val masterDevices = listOf<MasterDeviceDescriptor>( StubMasterDeviceDescriptor( "M1" ), StubMasterDeviceDescriptor( "M2" ))
        val connectedDevices = listOf<DeviceDescriptor>( StubDeviceDescriptor( "C1" ), StubDeviceDescriptor( "C2" ))
        val connections = listOf(
            StudyProtocolSnapshot.DeviceConnection("C1", "M1" ),
            StudyProtocolSnapshot.DeviceConnection( "C2", "M2" ) )
        val tasks = listOf<TaskDescriptor>( StubTaskDescriptor( "T1" ), StubTaskDescriptor( "T2" ) )
        val triggers = listOf(
            StudyProtocolSnapshot.TriggerWithId( 0, StubTrigger( masterDevices[ 0 ] ) ),
            StudyProtocolSnapshot.TriggerWithId( 1, StubTrigger( masterDevices[ 1 ] ) ) )
        val triggeredTasks = listOf(
            StudyProtocolSnapshot.TriggeredTask( 0, "T1", "C1" ),
            StudyProtocolSnapshot.TriggeredTask( 1, "T2", "C2" )
        )

        val snapshot = StudyProtocolSnapshot(
            "Owner", "Study",
            masterDevices, connectedDevices, connections,
            tasks, triggers, triggeredTasks )
        val reorganizedSnapshot = StudyProtocolSnapshot(
            "Owner", "Study",
            masterDevices.reversed(), connectedDevices.reversed(), connections.reversed(),
            tasks.reversed(), triggers.reversed(), triggeredTasks.reversed() )

        assertTrue( snapshot == reorganizedSnapshot )
        assertTrue( snapshot.hashCode() == reorganizedSnapshot.hashCode() )
    }
}