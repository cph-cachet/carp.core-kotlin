package dk.cachet.carp.data.application

import kotlin.test.*


/**
 * Tests for [SyncPoint].
 */
class SyncPointTest
{
    @Test
    fun forCurrentTimestamp_succeeds()
    {
        val doubleSpeed = SyncPoint.forCurrentTimestamp( 0, 2.0 )
        val zeroAtUTC = doubleSpeed.utcOffset

        assertEquals( zeroAtUTC + 500, doubleSpeed.synchronizeTimestamp( 1000 ) )
    }

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
