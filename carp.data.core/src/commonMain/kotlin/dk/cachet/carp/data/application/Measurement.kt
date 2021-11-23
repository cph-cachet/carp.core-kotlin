package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.data.infrastructure.getDataType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.Decoder


/**
 * The result of a measurement of [data] of a given [dataType] at a specific point or interval in time.
 * When [sensorEndTime] is set, the [data] pertains to an interval in time; otherwise, a point in time.
 *
 * The unit of [sensorStartTime] and [sensorEndTime] is fully determined by the sensor that collected the data.
 * For example, it could be a simple clock increment since the device powered up.
 */
@Serializable( MeasurementSerializer::class )
data class Measurement<out TData : Data>(
    val sensorStartTime: Long,
    val sensorEndTime: Long? = null,
    val dataType: DataType,
    val data: TData
)
{
    init
    {
        if ( sensorEndTime != null )
        {
            require( sensorEndTime >= sensorStartTime ) { "If set, sensorEndTime needs to lie after sensorStartTime." }
        }
    }

    /**
     * Determines whether the measured data pertains to a [DataTimeType.POINT] in time or a [DataTimeType.TIME_SPAN].
     */
    fun getDataTimeType(): DataTimeType =
        if ( sensorEndTime == null ) DataTimeType.POINT
        else DataTimeType.TIME_SPAN

    /**
     * Convert this [Measurement] to one synchronized to UTC time using [syncPoint].
     */
    fun synchronizeToUTC( syncPoint: SyncPoint ): Measurement<TData> =
        copy(
            sensorStartTime = syncPoint.synchronizeTimestamp( sensorStartTime ),
            sensorEndTime =
                if ( sensorEndTime == null ) sensorEndTime
                else syncPoint.synchronizeTimestamp( sensorEndTime )
        )
}


/**
 * A custom serializer for [Measurement] which omits the data type information as it is already
 * part of polymorphic serialization of the measured data.
 * In addition, it supports serializing star-projected `Measurement<*>` types.
 */
object MeasurementSerializer : KSerializer<Measurement<Data>>
{
    @Serializable
    private class Surrogate( val sensorStartTime: Long, val sensorEndTime: Long? = null, val data: Data )

    override val descriptor: SerialDescriptor = Surrogate.serializer().descriptor


    override fun serialize( encoder: Encoder, value: Measurement<Data> )
    {
        val surrogate = Surrogate( value.sensorStartTime, value.sensorEndTime, value.data )
        encoder.encodeSerializableValue( Surrogate.serializer(), surrogate )
    }

    override fun deserialize( decoder: Decoder ): Measurement<Data>
    {
        val surrogate = decoder.decodeSerializableValue( Surrogate.serializer() )
        val dataType = getDataType( surrogate.data::class )
        return Measurement( surrogate.sensorStartTime, surrogate.sensorEndTime, dataType, surrogate.data )
    }
}
