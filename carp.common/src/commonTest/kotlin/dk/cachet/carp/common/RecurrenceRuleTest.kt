package dk.cachet.carp.common

import dk.cachet.carp.common.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [RecurrenceRule].
 */
class RecurrenceRuleTest
{
    @Test
    fun cant_initialize_invalid_rule()
    {
        assertFailsWith<IllegalArgumentException> { RecurrenceRule( RecurrenceRule.Frequency.SECONDLY, 0 ) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule( RecurrenceRule.Frequency.MINUTELY, -1 ) }
        assertFailsWith<IllegalArgumentException>
        {
            RecurrenceRule( RecurrenceRule.Frequency.HOURLY, 1, RecurrenceRule.End.Count( 0 ) )
        }
        assertFailsWith<IllegalArgumentException>
        {
            RecurrenceRule( RecurrenceRule.Frequency.YEARLY, 1, RecurrenceRule.End.Count( -1 ) )
        }
    }

    @Test
    fun toString_is_RFC_5545_RRULE()
    {
        val defaults = RecurrenceRule( RecurrenceRule.Frequency.MONTHLY )
        assertEquals( "RRULE:FREQ=MONTHLY", defaults.toString() )

        val everyTwoDays = RecurrenceRule( RecurrenceRule.Frequency.DAILY, 2 )
        assertEquals( "RRULE:FREQ=DAILY;INTERVAL=2", everyTwoDays.toString() )

        val endInTwoHours = RecurrenceRule( RecurrenceRule.Frequency.HOURLY, 1, RecurrenceRule.End.Until( TimeSpan.fromMinutes( 120.0 ) ) )
        assertEquals( "RRULE:FREQ=HOURLY;UNTIL=7200000000", endInTwoHours.toString() )

        val twiceEverySecondWeek = RecurrenceRule( RecurrenceRule.Frequency.WEEKLY, 2, RecurrenceRule.End.Count( 2 ) )
        assertEquals( "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=2", twiceEverySecondWeek.toString() )
    }

    @Test
    fun can_serialize_and_deserialize_RecurrenceRule_using_JSON()
    {
        val rule = RecurrenceRule( RecurrenceRule.Frequency.DAILY, 2, RecurrenceRule.End.Count( 5 ) )

        val json = createDefaultJSON()
        val serialized = json.stringify( RecurrenceRule.serializer(), rule )
        val parsed = json.parse( RecurrenceRule.serializer(), serialized )

        assertEquals( rule, parsed )
    }
}
