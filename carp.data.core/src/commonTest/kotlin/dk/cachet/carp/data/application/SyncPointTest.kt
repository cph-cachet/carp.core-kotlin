package dk.cachet.carp.data.application

import kotlin.test.*


/**
 * Tests for [SyncPoint].
 */
class SyncPointTest
{
    @Test
    fun synchronizeTimestamp_for_double_speed_clock_succeeds()
    {
        val doubleSpeed = createDoubleSpeedSyncPoint()

        val synchronized = doubleSpeed.synchronizeTimestamp( 1000 )
        assertEquals( 500, synchronized )
    }

    @Test
    fun synchronizeTimestamp_for_utc_syncpoint_succeeds()
    {
        val synchronized = SyncPoint.UTC.synchronizeTimestamp( 1000 )
        assertEquals( 1000, synchronized )
    }
}
