package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.*
import kotlin.time.Duration


/**
 * Tests for [RecurrenceRule].
 */
class RecurrenceRuleTest
{
    @Test
    fun cant_initialize_invalid_rule()
    {
        assertFailsWith<IllegalArgumentException> { RecurrenceRule.secondly( 0 ) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule.minutely( -1 ) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule.hourly( 1, RecurrenceRule.End.Count( 0 ) ) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule.yearly( 1, RecurrenceRule.End.Count( -1 ) ) }
    }

    @Test
    fun toString_is_RFC_5545_RRULE()
    {
        val defaults = RecurrenceRule.monthly()
        assertEquals( "RRULE:FREQ=MONTHLY", defaults.toString() )

        val everyTwoDays = RecurrenceRule.daily( 2 )
        assertEquals( "RRULE:FREQ=DAILY;INTERVAL=2", everyTwoDays.toString() )

        val endInTwoHours = RecurrenceRule.hourly( 1, RecurrenceRule.End.Until( Duration.minutes( 120 ) ) )
        assertEquals( "RRULE:FREQ=HOURLY;UNTIL=7200000000", endInTwoHours.toString() )

        val twiceEverySecondWeek = RecurrenceRule.weekly( 2, RecurrenceRule.End.Count( 2 ) )
        assertEquals( "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=2", twiceEverySecondWeek.toString() )
    }

    @Test
    fun can_serialize_and_deserialize_RecurrenceRule_using_JSON()
    {
        val rule = RecurrenceRule.daily( 2, RecurrenceRule.End.Count( 5 ) )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( RecurrenceRule.serializer(), rule )
        val parsed = json.decodeFromString( RecurrenceRule.serializer(), serialized )

        assertEquals( rule, parsed )
    }
}
