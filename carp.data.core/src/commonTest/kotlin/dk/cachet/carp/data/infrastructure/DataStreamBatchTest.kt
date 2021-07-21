package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.data.application.SyncPoint
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [DataStreamBatch] relying on core infrastructure.
 */
class DataStreamBatchTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )
    private val testDataStreamBatch = DataStreamBatch(
        0,
        listOf( measurement( StubData(), 0 ), measurement( StubData(), 1 ) ),
        listOf( 1 ),
        SyncPoint( Clock.System.now() )
    )

    @Test
    fun can_serialize_and_deserialize_DataStreamBatch()
    {
        val dataStreamBatch: DataStreamBatch<StubData> = testDataStreamBatch
        val serialized = json.encodeToString( DataStreamBatchSerializer, dataStreamBatch )
        @Suppress( "UNCHECKED_CAST" )
        val parsed: DataStreamBatch<StubData> =
            json.decodeFromString( DataStreamBatchSerializer, serialized ) as DataStreamBatch<StubData>

        assertEquals( dataStreamBatch, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_DataStreamBatch_polymorphic()
    {
        val dataStreamBatch: DataStreamBatch<Data> = testDataStreamBatch
        val serialized = json.encodeToString( DataStreamBatchSerializer, dataStreamBatch )
        val parsed: DataStreamBatch<Data> = json.decodeFromString( DataStreamBatchSerializer, serialized )

        assertEquals( dataStreamBatch, parsed )
    }
}
