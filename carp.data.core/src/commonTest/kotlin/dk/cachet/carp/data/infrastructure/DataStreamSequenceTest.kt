package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.DataStreamSequenceSerializer
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.SyncPoint
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [DataStreamSequence] relying on core infrastructure.
 */
class DataStreamSequenceTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )
    private val testDataStreamSequence =
        with(
            MutableDataStreamSequence(
                dataStreamId<StubData>( UUID.randomUUID(), "Device" ),
                0,
                listOf( 1 ),
                SyncPoint( Clock.System.now() )
            )
        )
        {
            appendMeasurements(
                measurement( StubData(), 0 ),
                measurement( StubData(), 1 )
            )
            this
        }

    @Test
    fun can_serialize_and_deserialize_DataStreamSequence()
    {
        val serialized = json.encodeToString( DataStreamSequenceSerializer, testDataStreamSequence )
        val parsed: DataStreamSequence = json.decodeFromString( DataStreamSequenceSerializer, serialized )

        assertEquals( testDataStreamSequence.getDataStreamPoints(), parsed.getDataStreamPoints() )
    }
}
