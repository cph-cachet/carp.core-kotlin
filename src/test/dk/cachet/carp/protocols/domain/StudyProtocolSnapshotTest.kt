package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.data.CustomDataType
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.CustomMeasure
import dk.cachet.carp.protocols.domain.triggers.*
import org.junit.jupiter.api.*
import org.junit.Assert.*


/**
 * Tests for [StudyProtocolSnapshot].
 */
class StudyProtocolSnapshotTest
{
    @Test
    fun `equals and hashcode when comparing snapshots of same protocol`()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        val snapshot1: StudyProtocolSnapshot = protocol.getSnapshot()
        val snapshot2: StudyProtocolSnapshot = protocol.getSnapshot()

        assertTrue( snapshot1 == snapshot2 )
        assertTrue( snapshot1.hashCode() == snapshot2.hashCode() )
    }

    @Test
    fun `equals and hashcode when comparing snapshots of modified protocol`()
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
    fun `can (de)serialize snapshot using JSON`()
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
    fun `unknown types are wrapped when deserializing`()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()

        val parsed = StudyProtocolSnapshot.fromJson( serialized )
        assertEquals( 1, parsed.masterDevices.filter { m -> m is CustomMasterDeviceDescriptor }.count() )
        assertEquals( 1, parsed.connectedDevices.filter { m -> m is CustomDeviceDescriptor }.count() )
        assertEquals( 1, parsed.tasks.filter { m -> m is CustomTaskDescriptor }.count() )
        val allMeasures = parsed.tasks.flatMap{ t -> t.measures }
        assertEquals( 1, allMeasures.filter { m -> m is CustomMeasure }.count() )
        assertEquals( 1, allMeasures.map { m -> m.type }.filter { t -> t is CustomDataType }.count() )
        assertEquals( 1, parsed.triggers.filter { t -> t.trigger is CustomTrigger }.count() )
    }

    @Test
    fun `unknown connected master device is deserialized as a master device`()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        protocol.addMasterDevice( master )
        val unknownMaster = UnknownMasterDeviceDescriptor( "Unknown master" )
        protocol.addConnectedDevice( unknownMaster, master )

        var serialized = protocol.getSnapshot().toJson()
        serialized = serialized.replace( UnknownMasterDeviceDescriptor::class.qualifiedName!!, "com.unknown.CustomMasterDevice" )

        val parsed = StudyProtocolSnapshot.fromJson( serialized )
        assert( parsed.connectedDevices.single() is MasterDeviceDescriptor )
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun `serializing unknown types removes the wrapper`()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot = StudyProtocolSnapshot.fromJson( serialized )

        val customSerialized = snapshot.toJson()
        assertEquals( serialized, customSerialized )
    }

    @Test
    fun `order of elements in snapshot does not matter for equality or hashcode`()
    {
        val masterDevices = arrayOf<MasterDeviceDescriptor>( StubMasterDeviceDescriptor( "M1" ), StubMasterDeviceDescriptor( "M2" ))
        val connectedDevices = arrayOf<DeviceDescriptor>( StubDeviceDescriptor( "C1" ), StubDeviceDescriptor( "C2" ))
        val connections = arrayOf(
            StudyProtocolSnapshot.DeviceConnection("C1", "M1" ),
            StudyProtocolSnapshot.DeviceConnection( "C2", "M2" ) )
        val tasks = arrayOf<TaskDescriptor>( StubTaskDescriptor( "T1" ), StubTaskDescriptor( "T2" ) )
        val triggers = arrayOf(
            StudyProtocolSnapshot.TriggerWithId( 0, StubTrigger( masterDevices[ 0 ] ) ),
            StudyProtocolSnapshot.TriggerWithId( 1, StubTrigger( masterDevices[ 1 ] ) ) )
        val triggeredTasks = arrayOf(
            StudyProtocolSnapshot.TriggeredTask( 0, "T1", "C1" ),
            StudyProtocolSnapshot.TriggeredTask( 1, "T2", "C2" )
        )

        val snapshot = StudyProtocolSnapshot(
            "Owner", "Study",
            masterDevices, connectedDevices, connections,
                tasks, triggers, triggeredTasks )
        val reorganizedSnapshot = StudyProtocolSnapshot(
            "Owner", "Study",
            masterDevices.reversed().toTypedArray(), connectedDevices.reversed().toTypedArray(), connections.reversed().toTypedArray(),
            tasks.reversed().toTypedArray(), triggers.reversed().toTypedArray(), triggeredTasks.reversed().toTypedArray() )

        assertTrue( snapshot == reorganizedSnapshot )
        assertTrue( snapshot.hashCode() == reorganizedSnapshot.hashCode() )
    }
}