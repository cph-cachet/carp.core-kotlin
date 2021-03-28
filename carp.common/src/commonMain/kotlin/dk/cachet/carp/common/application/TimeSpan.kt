package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpLongPrimitiveSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


/**
 * Represents a time interval.
 * TODO: A custom serializer which serializes [INFINITE] as text, and optionally defines metrics to use (e.g., ms), would make JSON more human readable.
 */
@Serializable( TimeSpanSerializer::class )
data class TimeSpan(
    /**
     * The duration of the time interval expressed in microseconds.
     */
    val microseconds: Long
)
{
    companion object
    {
        private const val MICROSECONDS_IN_MS: Long = 1000
        private const val MICROSECONDS_IN_S: Long = MICROSECONDS_IN_MS * 1000
        private const val MICROSECONDS_IN_M: Long = MICROSECONDS_IN_S * 60

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
        fun fromMilliseconds( ms: Double ): TimeSpan = TimeSpan( (ms * MICROSECONDS_IN_MS).toLong() )

        /**
         * Returns a [TimeSpan] that represents a specified number of seconds.
         *
         * The value parameter is converted to microseconds, and that number of microseconds is used to initialize the new [TimeSpan].
         * Therefore, value will only be considered accurate to the nearest microsecond.
         */
        fun fromSeconds( s: Double ): TimeSpan = TimeSpan( (s * MICROSECONDS_IN_S).toLong() )

        /**
         * Returns a [TimeSpan] that represents a specified number of minutes.
         *
         * The value parameter is converted to microseconds, and that number of microseconds is used to initialize the new [TimeSpan].
         * Therefore, value will only be considered accurate to the nearest microsecond.
         */
        fun fromMinutes( m: Double ): TimeSpan = TimeSpan( (m * MICROSECONDS_IN_M).toLong() )
    }

    /**
     * Gets the value of the current [TimeSpan] expressed in whole and fractional milliseconds.
     */
    val totalMilliseconds: Double get() = this.microseconds.toDouble() / MICROSECONDS_IN_MS

    /**
     * Gets the value of the current [TimeSpan] expressed in whole and fractional seconds.
     */
    val totalSeconds: Double get() = this.microseconds.toDouble() / MICROSECONDS_IN_S

    /**
     * Gets the value of the current [TimeSpan] expressed in whole and fractional minutes.
     */
    val totalMinutes: Double get() = this.microseconds.toDouble() / MICROSECONDS_IN_M
}


/**
 * A custom serializer for [TimeSpan].
 */
object TimeSpanSerializer : KSerializer<TimeSpan> by createCarpLongPrimitiveSerializer( { TimeSpan( it ) }, { it.microseconds } )
