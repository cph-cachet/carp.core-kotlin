package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.createStubSequence
import kotlin.test.*


/**
 * Tests for [DataStreamBatch] relying on core infrastructure.
 */
class DataStreamBatchTest
{
    @Test
    fun can_serialize_and_deserialize_DataStreamBatch()
    {
        val json = createDefaultJSON( STUBS_SERIAL_MODULE )

        val batch = MutableDataStreamBatch()
        batch.appendSequence( createStubSequence( 0, StubData() ) )
        batch.appendSequence( createStubSequence( 2, StubData() ) )
        batch.appendSequence( createStubSequence( 0, StubDataPoint() ) )

        val serialized = json.encodeToString( DataStreamBatchSerializer, batch )
        val parsed: DataStreamBatch = json.decodeFromString( DataStreamBatchSerializer, serialized )

        assertEquals( batch.toList(), parsed.toList() )
    }
}
