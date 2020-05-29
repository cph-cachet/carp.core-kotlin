package dk.cachet.carp.common

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer


/**
 * Represents the iCalendar RFC 5545 standard recurrence rule to specify repeating events:
 * https://tools.ietf.org/html/rfc5545#section-3.3.10
 * TODO: Add support for the remainder of the specification. But, the current implementation is a valid subset.
 *
 * However, since date times are relative to the start time of a study,
 * they are replaced with time spans representing elapsed time since the start of the study.
 */
@Serializable( with = RecurrenceRuleSerializer::class )
data class RecurrenceRule(
    /**
     * Specifies the type of interval at which to repeat events, or multiples thereof.
     */
    val frequency: Frequency,
    /**
     * The interval at which [frequency] repeats.
     * The default is 1. For example, with [Frequency.DAILY], a value of "8" means every eight days.
     */
    val interval: Int = 1,
    /**
     * Specifies when, if ever, to stop repeating events. Default recurrence is forever.
     */
    val end: End = End.Never
)
{
    companion object
    {
        fun secondly( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.SECONDLY, interval, end )
        fun minutely( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.MINUTELY, interval, end )
        fun hourly( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.HOURLY, interval, end )
        fun daily( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.DAILY, interval, end )
        fun weekly( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.WEEKLY, interval, end )
        fun monthly( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.MONTHLY, interval, end )
        fun yearly( interval: Int = 1, end: End = End.Never ) = RecurrenceRule( Frequency.YEARLY, interval, end )
    }

    init
    {
        require( interval >= 1 ) { "Interval needs to be 1 or more." }
    }

    /**
     * Specify repeating events based on an interval of a chosen type or multiples thereof.
     */
    enum class Frequency { SECONDLY, MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY }

    @Serializable
    sealed class End
    {
        /**
         * Bounds the recurrence rule in an inclusive manner to the associated start date of this rule plus [elapsedTime].
         */
        @Serializable
        data class Until( val elapsedTime: TimeSpan ) : End()
        {
            override fun toString(): String = "UNTIL=${elapsedTime.microseconds}"
        }

        /**
         * Specify a number of occurrences at which to range-bound the recurrence.
         * The start date time always counts as the first occurrence.
         */
        @Serializable
        data class Count( val count: Int ) : End()
        {
            init { require( count >= 1 ) { "Count needs to be 1 or more." } }

            override fun toString(): String = "COUNT=$count"
        }

        /**
         * The recurrence repeats forever.
         */
        @Serializable
        object Never : End()
    }


    /**
     * A valid RFC 5545 string representation of this recurrence rule, except when [end] is specified as [End.Until].
     * When [End.Until] is specified, 'UNTIL' holds the total number of microseconds which need to be added to a desired start date.
     * 'UNTIL' should be reassigned to a calculated end date time, formatted using the RFC 5545 specifications: https://tools.ietf.org/html/rfc5545#section-3.3.5
     */
    override fun toString(): String
    {
        var rule = "RRULE:FREQ=$frequency"
        if ( interval != 1 ) rule = rule.plus( ";INTERVAL=$interval" )
        if ( end != End.Never ) rule = rule.plus( ";$end" )

        return rule
    }
}


/**
 * Regular expression to verify whether the structure of the string representation of a [RecurrenceRule] is valid.
 */
val RecurrenceRuleRegex = Regex( """RRULE:FREQ=(SECONDLY|MINUTELY|HOURLY|DAILY|WEEKLY|MONTHLY|YEARLY)(;(INTERVAL|UNTIL|COUNT)=\d+)*""" )


/**
 * A custom serializer for [RecurrenceRule].
 */
@Serializer( forClass = RecurrenceRule::class )
object RecurrenceRuleSerializer : KSerializer<RecurrenceRule>
{
    override fun serialize( encoder: Encoder, value: RecurrenceRule ) =
        encoder.encodeString( value.toString() )

    override fun deserialize( decoder: Decoder ): RecurrenceRule
    {
        val rule = decoder.decodeString()
        require( RecurrenceRuleRegex.matches( rule ) ) { "Invalid or unsupported RecurrenceRule string representation." }

        // Extract parameters.
        val parameters = rule.substring( "RRULE:".length )
            .split( ';' )
            .map {
                val par = it.split( '=' )
                require( par.count() == 2 ) { "Invalid RRULE parameter format." }
                par[ 0 ] to par[ 1 ]
            }
            .toMap()

        // Verify parameter correctness.
        val supportedParameters = listOf( "FREQ", "INTERVAL", "UNTIL", "COUNT" )
        require( parameters.keys.all { it in supportedParameters } ) { "Invalid or unsupported RRULE parameter found." }
        require( parameters.keys.distinct().count() == parameters.keys.count() ) { "RRULE does not allow repeating the same parameter multiple times." }

        // Extract frequency.
        val frequencyString = parameters[ "FREQ" ] ?: throw IllegalArgumentException( "FREQ needs to be specified." )
        val frequency = RecurrenceRule.Frequency.valueOf( frequencyString )

        // Extract remaining parameters.
        var interval: Int = 1
        var until: TimeSpan? = null
        var count: Int? = null
        for ( par in parameters )
        {
            when ( par.key )
            {
                "INTERVAL" -> interval = par.value.toInt()
                "UNTIL" -> until = TimeSpan( par.value.toLong() )
                "COUNT" -> count = par.value.toInt()
            }
        }
        require( until == null || count == null ) { "UNTIL and COUNT cannot both be set." }

        // Determine end.
        val end =
            if ( until == null && count == null ) RecurrenceRule.End.Never
            else
            {
                if ( until != null ) RecurrenceRule.End.Until( until )
                else RecurrenceRule.End.Count( count!! )
            }

        return RecurrenceRule( frequency, interval, end )
    }
}
