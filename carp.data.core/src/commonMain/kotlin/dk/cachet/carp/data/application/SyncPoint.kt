package dk.cachet.carp.data.application

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * Information about a sensor clock at [utcTime] on a master device
 * which allows converting sensor timestamps to UTC time.
 *
 * The required units/sign are determined by the formula: (sensorTimeStamp * [relativeClockSpeed]) + [utcOffset]
 */
@Serializable
data class SyncPoint(
    /**
     * The UTC time as measured on the master device when it determined the synchronization point.
     */
    val utcTime: Instant,
    /**
     * The offset to be added to the sensor time stamps after having multiplied by [relativeClockSpeed].
     */
    val utcOffset: Long = 0,
    /**
     * The value to multiply sensor time stamps by.
     */
    val relativeClockSpeed: Double = 1.0
)
