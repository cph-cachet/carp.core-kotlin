package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.createStubSequence
import dk.cachet.carp.data.application.stubDeploymentId
import dk.cachet.carp.data.application.stubSequenceDeviceRoleName
import dk.cachet.carp.data.application.stubSyncPoint
import dk.cachet.carp.data.application.stubTriggerIds
import kotlin.test.*


/**
 * Tests for helper functions defined in `SerializerDerivedMeasurement.kt`.
 */
class SerializerDerivedMethodsTest
{
    private val noRegistrations = object : DataTypeMetaDataMap() { }


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
    fun isValidMeasurement_returns_unknown_when_DataType_is_not_registered()
    {
        val unregisteredType = measurement( StubDataPoint(), 0 )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( unregisteredType ) )
    }

    @Test
    fun isValidDataStreamSequence_when_point_measurements_are_valid_and_ordered()
    {
        val measurement1 = measurement( StubDataPoint(), 0 )
        val measurement2 = measurement( StubDataPoint(), 10 )
        val sequence = createStubSequence( 0, measurement1, measurement2 )

        assertEquals( Trilean.TRUE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( sequence) )
    }

    @Test
    fun isValidDataStreamSequence_when_time_span_measurements_are_valid_and_ordered()
    {
        val measurement1 = measurement( StubDataTimeSpan(), 0, 10 )
        val measurement2 = measurement( StubDataTimeSpan(), 0, 15 )
        val sequence = createStubSequence( 0, measurement1, measurement2 )

        assertEquals( Trilean.TRUE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( sequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_there_are_no_measurements()
    {
        val emptySequence = MutableDataStreamSequence(
            dataStreamId<StubDataPoint>( stubDeploymentId, stubSequenceDeviceRoleName ),
            0,
            stubTriggerIds,
            stubSyncPoint
        )

        assertEquals( Trilean.TRUE, StubDataTypes.isValidDataStreamSequence( emptySequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( emptySequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_there_are_invalid_measurements()
    {
        val incorrectMeasurement = measurement( StubDataPoint(), 0 )
        val correctMeasurement = measurement( StubDataPoint(), 10, 15 )
        val sequence = createStubSequence( 0, incorrectMeasurement, correctMeasurement )

        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.FALSE, noRegistrations.isValidDataStreamSequence( sequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_point_measurements_are_ordered_incorrectly()
    {
        val measurement1 = measurement( StubDataPoint(), 10 )
        val measurement2 = measurement( StubDataPoint(), 0 )
        val sequence = createStubSequence( 0, measurement1, measurement2 )

        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.FALSE, noRegistrations.isValidDataStreamSequence( sequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_time_span_measurements_are_ordered_incorrectly()
    {
        val measurement1 = measurement( StubDataTimeSpan(), 0, 10 )
        val measurement2 = measurement( StubDataTimeSpan(), 0, 5 )
        val sequence = createStubSequence( 0, measurement1, measurement2 )

        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.FALSE, noRegistrations.isValidDataStreamSequence( sequence ) )
    }
}
