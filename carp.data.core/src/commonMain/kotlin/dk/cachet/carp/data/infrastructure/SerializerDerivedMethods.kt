package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.application.toTrilean
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.Measurement
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import kotlin.reflect.KClass


/**
 * Get the corresponding [DataType] as determined by the [KSerializer] associated with [dataKlass].
 *
 * @throws IllegalArgumentException when no [KSerializer] is associated with [dataKlass].
 */
@OptIn( InternalSerializationApi::class, ExperimentalSerializationApi::class )
fun <TData : Data> getDataType( dataKlass: KClass<TData> ): DataType =
    try
    {
        DataType.fromString( dataKlass.serializer().descriptor.serialName )
    }
    catch ( _: SerializationException )
    {
        throw IllegalArgumentException( "\"$dataKlass\" isn't a serializable Data class." )
    }

/**
 * Initialize a [DataStreamId] with the specified [studyDeploymentId] and [deviceRoleName].
 * The [DataType] is extracted from the serializer associated with the class of [TData].
 */
inline fun <reified TData : Data> dataStreamId( studyDeploymentId: UUID, deviceRoleName: String ): DataStreamId =
    DataStreamId( studyDeploymentId, deviceRoleName, getDataType( TData::class ) )

/**
 * Initialize a [Measurement] with the specified [sensorStartTime] and [sensorEndTime] for [data].
 * The [DataType] is extracted from the serializer associated with the class of [TData].
 */
inline fun <reified TData : Data> measurement(
    data: TData,
    sensorStartTime: Long,
    sensorEndTime: Long? = null
): Measurement<TData> = Measurement( sensorStartTime, sensorEndTime, getDataType( TData::class ), data )

/**
 * Determines whether the [DataType] and [DataTimeType] of [measurement] corresponds to the expected values for [Data]
 * as determined by [DataTypeMetaDataMap], or [Trilean.UNKNOWN] in case the type of [Data] is not registered.
 */
fun DataTypeMetaDataMap.isValidMeasurement( measurement: Measurement<*> ): Trilean
{
    val expectedDataType = getDataType( measurement.data::class )
    val registeredType = this[ expectedDataType ] ?: return Trilean.UNKNOWN

    val expectedTimeType = registeredType.timeType
    val isValid =
        expectedDataType == measurement.dataType &&
        expectedTimeType == measurement.getDataTimeType()

    return isValid.toTrilean()
}

/**
 * Determines whether all [Measurement]s in [sequence] are valid as determined by [DataTypeMetaDataMap],
 * and all timestamps are ordered correctly.
 * If data type isn't registered in [DataTypeMetaDataMap], [Trilean.UNKNOWN] is returned if measurements
 * all share the same [DataTimeType] and are ordered correspondingly; [Trilean.FALSE] otherwise.
 */
fun DataTypeMetaDataMap.isValidDataStreamSequence( sequence: DataStreamSequence ): Trilean
{
    val expectedDataType = sequence.dataStream.dataType
    val registeredType: DataTypeMetaData? = this[ expectedDataType ]

    // Early out for empty collections.
    if ( sequence.measurements.isEmpty() ) return if ( registeredType == null ) Trilean.UNKNOWN else Trilean.TRUE

    // Return false if the first measurement has a different time type than expected.
    val first = sequence.measurements.first()
    val expectedTimeType = registeredType?.timeType ?: first.getDataTimeType()
    if ( registeredType != null && first.getDataTimeType() != expectedTimeType ) return Trilean.FALSE

    // Return false if any of the remaining measurements has an invalid time type or are ordered incorrectly.
    sequence.measurements.reduce { cur, next ->
        val isValid = next.getDataTimeType() == expectedTimeType &&
            when( expectedTimeType )
            {
                DataTimeType.POINT -> next.sensorStartTime > cur.sensorStartTime
                DataTimeType.TIME_SPAN ->
                    next.sensorStartTime >= cur.sensorStartTime &&
                    next.sensorEndTime!! > cur.sensorEndTime!!
            }
        if ( !isValid ) return Trilean.FALSE
        next
    }

    // No invalid ordering found, so sequence is known to be valid if the type is known.
    return if ( registeredType == null ) Trilean.UNKNOWN else Trilean.TRUE
}
