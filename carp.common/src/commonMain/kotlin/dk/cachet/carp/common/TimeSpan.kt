package dk.cachet.carp.common

import kotlinx.serialization.Serializable


/**
 * Represents a time interval.
 * TODO: A custom serializer which serializes [INFINITE] as text, and optionally defines metrics to use (e.g., ms), would make JSON more human readable.
 */
@Serializable
data class TimeSpan(
    /**
     * The duration of the time interval expressed in microseconds.
     */
    val microseconds: Long )
{
    companion object
    {
        private const val MICROSECONDS_IN_MS: Long = 1000

        /**
         * A constant used to specify an infinite time 'interval'.
         * This is equivalent to -1 millisecond, similar to .NET.
         */
        val INFINITE: TimeSpan = TimeSpan( -MICROSECONDS_IN_MS )

        /**
         * Returns a [TimeSpan] that represents a specified number of milliseconds.
         *
         * The value parameter is converted to microseconds, and that number of microseconds is used to initialize the new [TimeSpan].
         * Therefore, value will only be considered accurate to the nearest microsecond.
         */
        fun fromMilliseconds( ms: Double ): TimeSpan
        {
            val ticks: Long = (ms * MICROSECONDS_IN_MS).toLong()
            return TimeSpan( ticks )
        }
    }

    /**
     * Gets the value of the current [TimeSpan] expressed in whole and fractional milliseconds.
     */
    val totalMilliseconds: Double get() = this.microseconds.toDouble() / MICROSECONDS_IN_MS
}
