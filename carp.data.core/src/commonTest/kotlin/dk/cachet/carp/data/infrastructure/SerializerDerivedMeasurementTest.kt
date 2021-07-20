package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import dk.cachet.carp.data.application.Measurement
import kotlin.test.*


/**
 * Tests for helper functions defined in `SerializerDerivedMeasurement.kt`.
 */
class SerializerDerivedMeasurementTest
{
    @Test
    fun getDataType_succeeds()
    {
        val stubDataType = getDataType( StubData::class )
        assertEquals( STUB_DATA_TYPE, stubDataType )
    }

    @Test
    fun measurement_succeeds()
    {
        val stub = measurement( StubData(), 0 )
        assertEquals( STUB_DATA_TYPE, stub.dataType )
    }

    @Test
    fun isValidMeasurement_returns_true_when_DataTimeType_corresponds()
    {
        val either = measurement( StubData(), 0, 0 )
        val point = measurement( StubDataPoint(), 0 )
        val timeSpan = measurement( StubDataTimeSpan(), 0, 1 )

        for ( data in listOf( either, point, timeSpan ) )
        {
            assertEquals( Trilean.TRUE, StubDataTypes.isValidMeasurement( data ) )
        }
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
        val incorrectDataType = Measurement( 0, null, STUB_DATA_TYPE, StubDataPoint() )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( incorrectDataType ) )
    }

    @Test
    fun isValidMeasurement_returns_unknown_when_Data_type_is_not_registered()
    {
        val noRegistrations = object : DataTypeMetaDataMap() { }

        val unregisteredType = measurement( StubData(), 0 )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( unregisteredType ) )
    }
}
