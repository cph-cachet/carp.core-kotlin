package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.MeasurementSerializer
import kotlin.test.*


/**
 * Tests for [Measurement] relying on core infrastructure.
 */
class MeasurementTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )


    @Test
    fun can_serialize_and_deserialize_Measurement()
    {
        val measurement: Measurement<StubDataPoint> = measurement( StubDataPoint(), 0 )
        val serialized = json.encodeToString( MeasurementSerializer, measurement )
        @Suppress( "UNCHECKED_CAST" )
        val parsed: Measurement<StubDataPoint> =
            json.decodeFromString( MeasurementSerializer, serialized ) as Measurement<StubDataPoint>

        assertEquals( measurement, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_Measurement_polymorphic()
    {
        val measurement: Measurement<Data> = measurement( StubDataPoint(), 0 )
        val serialized = json.encodeToString( MeasurementSerializer, measurement )
        val parsed: Measurement<Data> = json.decodeFromString( MeasurementSerializer, serialized )

        assertEquals( measurement, parsed )
    }
}
