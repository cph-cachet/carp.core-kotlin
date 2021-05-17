package dk.cachet.carp.common.application

import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for [TimeSpan].
 */
class TimeSpanTest
{
    @Test
    fun fromMilliseconds_with_whole_numbers_succeeds()
    {
        val ten = TimeSpan.fromMilliseconds( 10.0 )
        assertEquals( 10.0, ten.totalMilliseconds )
    }

    @Test
    fun fromSeconds_with_whole_numbers_succeeds()
    {
        val six = TimeSpan.fromSeconds( 6.0 )
        assertEquals( 6.0, six.totalSeconds )
    }

    @Test
    fun fromMinutes_with_whole_numbers_succeeds()
    {
        val halfHour = TimeSpan.fromMinutes( 30.0 )
        assertEquals( 30.0, halfHour.totalMinutes )
    }

    @Test
    fun totalMilliseconds_with_fraction_succeeds()
    {
        val halfMs = TimeSpan( 500 )
        assertEquals( 0.5, halfMs.totalMilliseconds )
    }
}
