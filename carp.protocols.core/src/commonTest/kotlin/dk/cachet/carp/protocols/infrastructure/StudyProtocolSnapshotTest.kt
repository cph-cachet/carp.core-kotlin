package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.domain.StudyProtocol
import dk.cachet.carp.common.application.StudyProtocolSnapshot
import dk.cachet.carp.common.application.devices.MasterDeviceDescriptor
import dk.cachet.carp.common.application.tasks.measures.Measure
import dk.cachet.carp.common.infrastructure.serialization.CustomDeviceDescriptor
import dk.cachet.carp.common.infrastructure.serialization.CustomMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.serialization.CustomMeasure
import dk.cachet.carp.common.infrastructure.serialization.CustomTaskDescriptor
import dk.cachet.carp.common.infrastructure.serialization.CustomTrigger
import dk.cachet.carp.common.infrastructure.serialization.fromJson
import dk.cachet.carp.common.infrastructure.serialization.toJson
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMeasure
import dk.cachet.carp.common.infrastructure.test.StubSamplingConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import dk.cachet.carp.common.infrastructure.test.createComplexProtocol
import dk.cachet.carp.common.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import kotlinx.serialization.ExperimentalSerializationApi
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
    @ExperimentalSerializationApi
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

    @ExperimentalSerializationApi
    @Test
    fun unknown_connected_master_device_is_deserialized_as_a_master_device()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Master" )
        protocol.addMasterDevice( master )
        val unknownMaster = StubMasterDeviceDescriptor( "Unknown master" )
        protocol.addConnectedDevice( unknownMaster, master )

        // Mimic unknown connected master device.
        var serialized = protocol.getSnapshot().toJson()
        serialized = serialized.makeUnknown( unknownMaster, "Unknown master" )

        val parsed = StudyProtocolSnapshot.fromJson( serialized )
        assertTrue { parsed.connectedDevices.single() is MasterDeviceDescriptor }
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @ExperimentalSerializationApi
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot = StudyProtocolSnapshot.fromJson( serialized )

        val customSerialized = snapshot.toJson()
        assertEquals( serialized, customSerialized )
    }

    @ExperimentalSerializationApi
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
    @ExperimentalSerializationApi
    private fun serializeProtocolSnapshotIncludingUnknownTypes(): String
    {
        val protocol = createComplexProtocol()

        // (1) Add unknown master with unknown sampling configuration and unknown connected device.
        val unknownSamplingConfiguration = StubSamplingConfiguration( "Unknown" )
        val samplingConfiguration = mapOf(
            STUB_DATA_TYPE to unknownSamplingConfiguration
        )
        val master = StubMasterDeviceDescriptor( "Unknown", samplingConfiguration )
        protocol.addMasterDevice( master )
        val connected = StubDeviceDescriptor( "Unknown 2" )
        protocol.addConnectedDevice( connected, master )

        // (2) Add unknown task with unknown measure.
        val unknownMeasure = StubMeasure( STUB_DATA_TYPE, "Unknown" )
        val measures: List<Measure> = listOf( unknownMeasure, StubMeasure( STUB_DATA_TYPE ) )
        val task = StubTaskDescriptor( "Unknown task", measures )
        val trigger = StubTrigger( master.roleName, "Unknown" )
        protocol.addTriggeredTask( trigger, task, master )

        // (3) Add known task with unknown measure.
        val unknownMeasure2 = StubMeasure( STUB_DATA_TYPE, "Unknown 2" )
        val task2 = StubTaskDescriptor( "Known task", listOf( unknownMeasure2 ) )
        protocol.addTriggeredTask( trigger, task2, master )

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        var serialized: String = snapshot.toJson()

        // Replace the strings which identify the types to load by the PolymorphicSerializer.
        // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
        serialized = serialized.makeUnknown( master, "Unknown" )
        serialized = serialized.makeUnknown( connected )
        serialized = serialized.makeUnknown( unknownSamplingConfiguration, "configuration", "Unknown" )
        serialized = serialized.makeUnknown( task )
        serialized = serialized.makeUnknown( unknownMeasure, "uniqueProperty", "Unknown" )
        serialized = serialized.makeUnknown( unknownMeasure, "uniqueProperty", "Unknown 2" )
        serialized = serialized.makeUnknown( trigger, "uniqueProperty", "Unknown" )

        return serialized
    }
}
