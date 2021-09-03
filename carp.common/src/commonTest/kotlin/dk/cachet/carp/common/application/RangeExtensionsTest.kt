package dk.cachet.carp.common.application

import kotlin.test.*


/**
 * Tests for extension functions in `RangeExtensions.kt`.
 */
class RangeExtensionsTest
{
    @Test
    fun intersect_for_contained_ranges()
    {
        val range = 0..10

        val containedRanges = listOf( 0..0, 0..1, 2..9, 5..5, 5..10, 0..10, 10..10 )
        for ( containedRange in containedRanges )
        {
            assertEquals( containedRange, range.intersect( containedRange ) )
            assertEquals( containedRange, containedRange.intersect( range ) )
        }
    }

    @Test
    fun intersect_empty_for_outside_ranges()
    {
        val range = 0..10

        val outsideRanges = listOf( -10..-10, -10..-1, 11..20, 20..20 )
        for ( outsideRange in outsideRanges )
        {
            assertTrue( range.intersect( outsideRange ).isEmpty() )
            assertTrue( outsideRange.intersect( range ).isEmpty() )
        }
    }

    @Test
    fun intersect_for_partial_overlap()
    {
        val range = 0..10

        val leftOverlap = -5..5
        assertEquals( 0..5, range.intersect( leftOverlap ) )
        assertEquals( 0..5, leftOverlap.intersect( range ) )

        val rightOverlap = 5..15
        assertEquals( 5..10, range.intersect( rightOverlap ) )
        assertEquals( 5..10, rightOverlap.intersect( range ) )
    }

    @Test
    fun intersect_for_empty_ranges()
    {
        val range = 0..10
        assertTrue( range.intersect( IntRange.EMPTY ).isEmpty() )
        assertTrue( IntRange.EMPTY.intersect( range ).isEmpty() )

        assertTrue( IntRange.EMPTY.intersect( IntRange.EMPTY ).isEmpty() )
    }
}
