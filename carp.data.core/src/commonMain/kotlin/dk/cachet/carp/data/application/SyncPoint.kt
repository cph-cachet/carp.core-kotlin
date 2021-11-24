package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Information about a sensor clock at [synchronizedOn] on a master device
 * which allows converting sensor time to UTC time in microseconds.
 *
 * The required units/sign to convert to UTC microseconds are determined by the formula:
 * syncedTime = [synchronizedOn] + (sensorTime + [utcOffset] - [synchronizedOn]) / [relativeClockSpeed]
 */
@Serializable
data class SyncPoint(
    /**
     * The UTC time as measured on the master device when it determined the synchronization point.
     */
    val synchronizedOn: Instant,
    /**
     * UTC time in microseconds minus sensor time at the moment in time defined by [synchronizedOn].
     */
    @Required
    val utcOffset: Long = 0,
    /**
     * The relative clock speed of the sensor clock compared to UTC time incrementing by microseconds.
     * E.g., if the sensor clock increments by 2 every microsecond, the relative clock speed is 2.
     */
    @Required
    val relativeClockSpeed: Double = 1.0
)
{
    companion object
    {
        /**
         * The default [SyncPoint] for sensors of which the clock is already synchronized to UTC time in microseconds.
         * A synchronization conversion using this sync point is a no-op.
         */
        val UTC: SyncPoint = SyncPoint( Instant.fromEpochSeconds( 0 ) )

        /**
         * Create a [SyncPoint] at the current point in time coinciding with a sensor measurement reported at [timestamp]
         * for a sensor clock running at [relativeClockSpeed].
         */
        fun forCurrentTimestamp( timestamp: Long, relativeClockSpeed: Double = 1.0 ): SyncPoint
        {
            val now = Clock.System.now()
            return SyncPoint( now, now.toEpochMicroseconds() - timestamp, relativeClockSpeed )
        }
    }


    /**
     * Converts [timestamp] to UTC time in microseconds using this [SyncPoint].
     */
    fun synchronizeTimestamp( timestamp: Long ): Long
    {
        val syncedOn = synchronizedOn.toEpochMicroseconds()
        val synced = syncedOn + (timestamp + utcOffset - syncedOn) / relativeClockSpeed

        return synced.toLong()
    }
}
