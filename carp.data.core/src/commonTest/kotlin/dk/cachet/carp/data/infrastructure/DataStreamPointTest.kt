package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.data.application.DataStreamPoint
import dk.cachet.carp.data.application.DataStreamPointSerializer
import dk.cachet.carp.data.application.SyncPoint
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [DataStreamPoint] relying on core infrastructure.
 */
class DataStreamPointTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )
    private val testDataStreamPoint = DataStreamPoint(
        0,
        UUID.randomUUID(),
        "Device",
        measurement( StubData(), 0 ),
        listOf( 0 ),
        SyncPoint( Clock.System.now() )
    )

    @Test
    fun can_serialize_and_deserialize_DataStreamPoint()
    {
        val dataStreamPoint: DataStreamPoint<StubData> = testDataStreamPoint
        val serialized = json.encodeToString( DataStreamPointSerializer, dataStreamPoint )
        @Suppress( "UNCHECKED_CAST" )
        val parsed: DataStreamPoint<Data> =
            json.decodeFromString( DataStreamPointSerializer, serialized ) as DataStreamPoint<StubData>

        assertEquals( dataStreamPoint, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_DataStreamPoint_polymorphic()
    {
        val dataStreamPoint: DataStreamPoint<Data> = testDataStreamPoint
        val serialized = json.encodeToString( DataStreamPointSerializer, dataStreamPoint )
        val parsed: DataStreamPoint<Data> = json.decodeFromString( DataStreamPointSerializer, serialized )

        assertEquals( dataStreamPoint, parsed )
    }
}
