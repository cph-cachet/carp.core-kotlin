package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.application.toTrilean
import dk.cachet.carp.data.application.DataStreamId
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
        measurement.getDataTimeType().matches( expectedTimeType )

    return isValid.toTrilean()
}
