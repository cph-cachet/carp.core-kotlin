package dk.cachet.carp.data.application

import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Information about a sensor clock at the timestamp [synchronizedOn] on a master device
 * which allows converting sensor timestamps to UTC time in microseconds.
 *
 * The required units/sign to convert to UTC microseconds are determined by the formula:
 * (sensorTimeStamp * [relativeClockSpeed]) + [utcOffset]
 */
@Serializable
data class SyncPoint(
    /**
     * The UTC time as measured on the master device when it determined the synchronization point.
     */
    val synchronizedOn: Instant,
    /**
     * The offset to be added to the sensor time stamps after having multiplied by [relativeClockSpeed].
     */
    @Required
    val utcOffset: Long = 0,
    /**
     * The value to multiply sensor time stamps by.
     */
    @Required
    val relativeClockSpeed: Double = 1.0
)
{
    companion object
    {
        /**
         * The default [SyncPoint] for timestamps that are already synchronized to UTC time.
         * A synchronization conversion using this sync point is a no-op.
         */
        val UTC: SyncPoint = SyncPoint( Instant.fromEpochSeconds( 0 ) )
    }
}
