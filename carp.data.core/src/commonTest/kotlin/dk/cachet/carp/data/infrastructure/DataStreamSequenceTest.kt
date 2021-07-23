package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.DataStreamSequenceSerializer
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.SyncPoint
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [DataStreamSequence] relying on core infrastructure.
 */
class DataStreamSequenceTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )
    private val testDataStreamSequence = DataStreamSequence.fromMeasurements(
        DataStreamId( UUID.randomUUID(), "Device", STUB_DATA_TYPE ),
        0,
        listOf( measurement( StubData(), 0 ), measurement( StubData(), 1 ) ),
        listOf( 1 ),
        SyncPoint( Clock.System.now() )
    )

    @Test
    fun can_serialize_and_deserialize_DataStreamSequence()
    {
        val serialized = json.encodeToString( DataStreamSequenceSerializer, testDataStreamSequence )
        @Suppress( "UNCHECKED_CAST" )
        val parsed: DataStreamSequence<StubData> =
            json.decodeFromString( DataStreamSequenceSerializer, serialized ) as DataStreamSequence<StubData>

        assertEquals( testDataStreamSequence, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_DataStreamSequence_polymorphic()
    {
        val serialized = json.encodeToString( DataStreamSequenceSerializer, testDataStreamSequence )
        val parsed: DataStreamSequence<Data> = json.decodeFromString( DataStreamSequenceSerializer, serialized )

        assertEquals( testDataStreamSequence, parsed )
    }
}
