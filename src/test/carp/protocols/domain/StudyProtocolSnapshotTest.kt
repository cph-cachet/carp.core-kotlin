package carp.protocols.domain

import carp.protocols.domain.devices.*
import carp.protocols.domain.tasks.*
import carp.protocols.domain.triggers.*
import kotlinx.serialization.SerialContext
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.*
import org.junit.Assert.*
import kotlin.test.assertFailsWith


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
        var protocol: StudyProtocol = createComplexProtocol()
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
        val serialized: String = JSON.stringify( snapshot )
        val parsed: StudyProtocolSnapshot = JSON.parse( serialized )

        assertEquals( snapshot, parsed )
    }

    /**
     * Currently, only types which are known at compile time are supported.
     * TODO: Types not known at compile time should not prevent deserializing a protocol, but should be loaded through an 'UnknownType' wrapper which extracts the base class information.
     */
    @Test
    fun `can't deserialize unknown types`()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        var serialized: String = JSON.stringify( protocol.getSnapshot() )
        serialized = serialized.replace( StubTaskDescriptor::class.qualifiedName!!, "carp.protocols.domain.tasks.UnknownTask" )

        assertFailsWith<ClassNotFoundException>
        {
            JSON.parse<StudyProtocolSnapshot>( serialized )
        }
    }
}