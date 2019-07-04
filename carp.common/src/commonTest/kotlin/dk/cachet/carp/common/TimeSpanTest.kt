package dk.cachet.carp.common

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
    fun totalMilliseconds_with_fraction_succeeds()
    {
        val halfMs = TimeSpan( 500 )
        assertEquals( 0.5, halfMs.totalMilliseconds )
    }
}