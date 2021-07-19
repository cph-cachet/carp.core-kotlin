package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.Serializable


/**
 * The result of a measurement of [data] of a given [dataType] at a specific point or interval in time.
 * When [sensorEndTime] is set, the [data] pertains to an interval in time; otherwise, a point in time.
 *
 * The unit of [sensorStartTime] and [sensorEndTime] is fully determined by the sensor that collected the data.
 * For example, it could be a simple clock increment since the device powered up.
 */
@Serializable
data class Measurement<out TData : Data>(
    val sensorStartTime: Long,
    val sensorEndTime: Long?,
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
}
