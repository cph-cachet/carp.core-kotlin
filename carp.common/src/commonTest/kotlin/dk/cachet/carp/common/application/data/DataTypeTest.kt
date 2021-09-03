package dk.cachet.carp.common.application.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Tests for types defined in `DataType.kt`.
 */
class DataTypeTest
{
    @Test
    fun DataTimeType_matches_succeeds()
    {
        val either = DataTimeType.EITHER
        val point = DataTimeType.POINT
        val timespan = DataTimeType.TIME_SPAN

        assertTrue( point.matches( either ) )
        assertTrue( timespan.matches( either ) )

        assertTrue( point.matches( point ) )
        assertFalse( timespan.matches( point ) )

        assertTrue( timespan.matches( timespan ) )
        assertFalse( point.matches( timespan ) )
    }
}
