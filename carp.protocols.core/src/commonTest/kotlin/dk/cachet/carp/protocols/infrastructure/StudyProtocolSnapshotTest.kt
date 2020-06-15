package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.UnknownDeviceDescriptor
import dk.cachet.carp.protocols.domain.UnknownMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.UnknownMeasure
import dk.cachet.carp.protocols.domain.UnknownSamplingConfiguration
import dk.cachet.carp.protocols.domain.UnknownTaskDescriptor
import dk.cachet.carp.protocols.domain.UnknownTrigger
import dk.cachet.carp.protocols.domain.createComplexProtocol
import dk.cachet.carp.protocols.domain.createEmptyProtocol
import dk.cachet.carp.protocols.domain.devices.CustomDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.CustomMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.CustomMeasure
import dk.cachet.carp.protocols.domain.tasks.CustomTaskDescriptor
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.domain.triggers.CustomTrigger
import dk.cachet.carp.protocols.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMeasure
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import kotlin.test.*


/**
 * Tests for [StudyProtocolSnapshot] relying on core infrastructure.
 */
class StudyProtocolSnapshotTest
{
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
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()

        val parsed = StudyProtocolSnapshot.fromJson( serialized )
        val masterDevice = parsed.masterDevices.filterIsInstance<CustomMasterDeviceDescriptor>().singleOrNull()
        assertNotNull( masterDevice )
        assertEquals( 1, masterDevice.samplingConfiguration.count() )
        assertEquals( 1, parsed.connectedDevices.filterIsInstance<CustomDeviceDescriptor>().count() )
        assertEquals( 1, parsed.tasks.filterIsInstance<CustomTaskDescriptor>().count() )
        val allMeasures = parsed.tasks.flatMap{ t -> t.measures }
        assertEquals( 2, allMeasures.filterIsInstance<CustomMeasure>().count() )
        assertEquals( 1, parsed.triggers.filter { t -> t.value is CustomTrigger }.count() )
    }

    @Test
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
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot = StudyProtocolSnapshot.fromJson( serialized )

        val customSerialized = snapshot.toJson()
        assertEquals( serialized, customSerialized )
    }

    @Test
    fun create_protocol_fromSnapshot_with_custom_extending_types_succeeds()
    {
        val serialized = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot = StudyProtocolSnapshot.fromJson( serialized )

        StudyProtocol.fromSnapshot( snapshot )
    }

    /**
     * Creates a study protocol which includes:
     * (1) an unknown master device with unknown sampling configuration and unknown connected device
     * (2) unknown task with an unknown measure and unknown data type, triggered by an unknown trigger
     * (3) known task with an unknown measure and known data type
     * There is thus exactly one unknown object for each of these types, except for 'Measure' which has two.
     */
    private fun serializeProtocolSnapshotIncludingUnknownTypes(): String
    {
        val protocol = createComplexProtocol()

        // (1) Add unknown master with unknown sampling configuration and unknown connected device.
        val samplingConfiguration = mapOf(
            STUB_DATA_TYPE to UnknownSamplingConfiguration( "Unknown" )
        )
        val master = UnknownMasterDeviceDescriptor( "Unknown", samplingConfiguration )
        protocol.addMasterDevice( master )
        val connected = UnknownDeviceDescriptor( "Unknown 2" )
        protocol.addConnectedDevice( connected, master )

        // (2) Add unknown task with unknown measure.
        val measures: List<Measure> = listOf( UnknownMeasure( STUB_DATA_TYPE ), StubMeasure( STUB_DATA_TYPE ) )
        val task = UnknownTaskDescriptor( "Unknown task", measures )
        val trigger = UnknownTrigger( master.roleName )
        protocol.addTriggeredTask( trigger, task, master )

        // (3) Add known task with unknown measure.
        val task2 = StubTaskDescriptor( "Known task", listOf( UnknownMeasure( STUB_DATA_TYPE ) ) )
        protocol.addTriggeredTask( trigger, task2, master )

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        var serialized: String = snapshot.toJson()

        // Replace the strings which identify the types to load by the PolymorphicSerializer.
        // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownMasterDeviceDescriptor", "com.unknown.CustomMasterDevice" )
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownDeviceDescriptor", "com.unknown.CustomDevice" )
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownSamplingConfiguration", "com.unknown.SamplingConfiguration" )
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownTaskDescriptor", "com.unknown.CustomTask" )
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownMeasure", "com.unknown.CustomMeasure" )
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.UnknownTrigger", "com.unknown.CustomTrigger" )

        return serialized
    }
}
