package dk.cachet.carp.common.application


/**
 * Returns the intersection of this range with the specified [range], or an empty range if there is no intersection.
 */
fun CharRange.intersect( range: CharRange ): CharRange =
    intersectRange( range ).let { CharRange( it.start, it.endInclusive ) }

/**
 * Returns the intersection of this range with the specified [range], or an empty range if there is no intersection.
 */
fun IntRange.intersect( range: IntRange ): IntRange =
    intersectRange( range ).let { IntRange( it.start, it.endInclusive ) }

/**
 * Returns the intersection of this range with the specified [range], or an empty range if there is no intersection.
 */
fun LongRange.intersect( range: LongRange ): LongRange =
    intersectRange( range ).let { LongRange( it.start, it.endInclusive ) }


/**
 * Returns the intersection of this range with the specified [range], or an empty range if there is no intersection.
 */
private fun <T : Comparable<T>> ClosedRange<T>.intersectRange( range: ClosedRange<T> ): ClosedRange<T>
{
    val startRange = maxOf( start, range.start )
    val endRange = minOf( endInclusive, range.endInclusive )

    return startRange..endRange
}
