package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import dk.cachet.carp.data.application.Measurement
import kotlin.test.*


/**
 * Tests for helper functions defined in `SerializerDerivedMeasurement.kt`.
 */
class SerializerDerivedMethodsTest
{
    @Test
    fun getDataType_succeeds()
    {
        val stubDataType = getDataType( StubDataPoint::class )
        assertEquals( STUB_DATA_POINT_TYPE, stubDataType )
    }

    @Test
    fun dataStreamId_succeeds()
    {
        val stream = dataStreamId<StubDataPoint>( UUID.randomUUID(), "Device" )

        assertEquals( STUB_DATA_POINT_TYPE, stream.dataType )
    }

    @Test
    fun measurement_succeeds()
    {
        val stub = measurement( StubDataPoint(), 0 )
        assertEquals( STUB_DATA_POINT_TYPE, stub.dataType )
    }

    @Test
    fun isValidMeasurement_returns_true_when_DataTimeType_corresponds()
    {
        val point = measurement( StubDataPoint(), 0 )
        assertEquals( Trilean.TRUE, StubDataTypes.isValidMeasurement( point ) )

        val timeSpan = measurement( StubDataTimeSpan(), 0, 1 )
        assertEquals( Trilean.TRUE, StubDataTypes.isValidMeasurement( timeSpan ) )
    }

    @Test
    fun isValidMeasurement_returns_false_when_DataTimeType_does_not_correspond()
    {
        val missingEndTime = measurement( StubDataTimeSpan(), 0 )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( missingEndTime ) )

        val noEndTimeAllowed = measurement( StubDataPoint(), 0, 1 )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( noEndTimeAllowed ) )
    }

    @Test
    fun isValidMeasurement_returns_false_when_DataType_does_not_correspond()
    {
        val incorrectDataType = Measurement( 0, null, STUB_DATA_POINT_TYPE, StubDataTimeSpan() )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( incorrectDataType ) )
    }

    @Test
    fun isValidMeasurement_returns_unknown_when_Data_type_is_not_registered()
    {
        val noRegistrations = object : DataTypeMetaDataMap() { }

        val unregisteredType = measurement( StubDataPoint(), 0 )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( unregisteredType ) )
    }
}
