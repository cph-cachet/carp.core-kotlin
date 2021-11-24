package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Information about a sensor clock at [synchronizedOn] on a master device
 * which allows converting sensor time to UTC time in microseconds.
 *
 * The required units/sign to convert to UTC microseconds are determined by the formula:
 * syncedTime = [relativeClockSpeed] * (sensorTime - [sensorTimestampAtSyncPoint]) + [synchronizedOn]
 */
@Serializable
data class SyncPoint(
    /**
     * The UTC time as measured on the master device when it determined the synchronization point.
     */
    val synchronizedOn: Instant,
    /**
     * The sensor time at [synchronizedOn].
     */
    @Required
    val sensorTimestampAtSyncPoint: Long = synchronizedOn.toEpochMicroseconds(),
    /**
     * The relative clock speed of UTC time compared to the sensor clock,
     * calculated as the variation of UTC time divided by the variation of sensor time.
     *
     * E.g., if the sensor clock runs half as fast as UTC time, the relative clock speed is 2.
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
    }


    /**
     * Converts [timestamp] to UTC time in microseconds using this [SyncPoint].
     */
    fun synchronizeTimestamp( timestamp: Long ): Long
    {
        val synced = relativeClockSpeed * (timestamp - sensorTimestampAtSyncPoint) +
            synchronizedOn.toEpochMicroseconds()

        return synced.toLong()
    }
}
