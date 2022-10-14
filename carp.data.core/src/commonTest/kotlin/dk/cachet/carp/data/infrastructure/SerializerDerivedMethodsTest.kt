package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.createStubSequence
import dk.cachet.carp.data.application.stubDeploymentId
import dk.cachet.carp.data.application.stubSequenceDeviceRoleName
import dk.cachet.carp.data.application.stubSyncPoint
import dk.cachet.carp.data.application.stubTriggerIds
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    fun isValidMeasurement_when_DataTimeType_corresponds_to_Data()
    {
        val point = measurement( StubDataPoint(), 0 )
        assertEquals( Trilean.TRUE, StubDataTypes.isValidMeasurement( point ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( point ) )

        val timeSpan = measurement( StubDataTimeSpan(), 0, 1 )
        assertEquals( Trilean.TRUE, StubDataTypes.isValidMeasurement( timeSpan ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( timeSpan ) )
    }

    @Test
    fun isValidMeasurement_when_DataTimeType_does_not_correspond()
    {
        val missingEndTime = measurement( StubDataTimeSpan(), 0 )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( missingEndTime ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( missingEndTime ) )

        val noEndTimeAllowed = measurement( StubDataPoint(), 0, 1 )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( noEndTimeAllowed ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( noEndTimeAllowed ) )
    }

    @Test
    fun isValidMeasurement_when_DataType_does_not_correspond_to_Data()
    {
        val incorrectDataType = Measurement( 0, null, STUB_DATA_POINT_TYPE, StubDataTimeSpan() )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidMeasurement( incorrectDataType ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidMeasurement( incorrectDataType ) )
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
        val emptySequence = MutableDataStreamSequence<StubDataPoint>(
            dataStreamId<StubDataPoint>( stubDeploymentId, stubSequenceDeviceRoleName ),
            0,
            stubTriggerIds,
            stubSyncPoint
        )

        assertEquals( Trilean.TRUE, StubDataTypes.isValidDataStreamSequence( emptySequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( emptySequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_measurement_time_types_are_inconsistent()
    {
        val measurement1 = measurement( StubDataPoint(), 0 )
        val measurement2 = measurement( StubDataPoint(), 10, 15 )
        val sequence = createStubSequence( 0, measurement1, measurement2 )

        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.FALSE, noRegistrations.isValidDataStreamSequence( sequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_measurements_time_types_are_consistent_and_ordered_but_wrong()
    {
        val measurement1 = measurement( StubDataTimeSpan(), 0 )
        val measurement2 = measurement( StubDataTimeSpan(), 10 )
        val sequence = createStubSequence( 0, measurement1, measurement2 )

        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( sequence ) )
    }

    @Test
    fun isValidDataStreamSequence_when_data_does_not_match_datatype()
    {
        // Wrong measurement is first in sequence.
        val dataType = STUB_DATA_POINT_TYPE
        val dataStreamId = DataStreamId( stubDeploymentId, "Test", dataType )
        val wrongMeasurement = Measurement<Data>( 10, null, dataType, StubDataTimeSpan() )
        val sequence = MutableDataStreamSequence<Data>( dataStreamId, 0, listOf( 1 ) )
        sequence.appendMeasurements( wrongMeasurement )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( sequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( sequence ) )

        // Wrong measurement appears later in sequence.
        val biggerSequence = MutableDataStreamSequence<Data>( dataStreamId, 0, listOf( 1 ) )
        biggerSequence.appendMeasurements( measurement( StubDataPoint(), 0 ) )
        biggerSequence.appendMeasurements( wrongMeasurement )
        assertEquals( Trilean.FALSE, StubDataTypes.isValidDataStreamSequence( biggerSequence ) )
        assertEquals( Trilean.UNKNOWN, noRegistrations.isValidDataStreamSequence( biggerSequence ) )
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

    private object TestObjects
    {
        const val additionalTypeName = "some.namespace.additional_type"
        val additionalType = DataType.fromString( additionalTypeName )
    }

    @Serializable
    @SerialName( TestObjects.additionalTypeName )
    data class AdditionalType( val ignore: String = "" ) : Data

    @Test
    fun validation_functions_can_operate_on_merged_maps()
    {
        val additionalType = DataType.fromString( TestObjects.additionalTypeName )
        val additionalTypes = mapOf(
            additionalType to DataTypeMetaData( additionalType, "Additional type", DataTimeType.POINT )
        )
        val mergedTypes = StubDataTypes + additionalTypes

        val measurement = Measurement( 0, null, TestObjects.additionalType, AdditionalType() )
        mergedTypes.isValidMeasurement( measurement )
    }
}
