package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [DataStreamPoint].
 */
class DataStreamPointTest
{
    @Test
    fun initializing_DataStreamPoint_with_negative_sequenceId_fails()
    {
        assertFailsWith<IllegalArgumentException> {
            DataStreamPoint(
                -1,
                UUID.randomUUID(),
                "Device",
                measurement( StubData(), 0 ),
                listOf( 1 ),
                SyncPoint( Clock.System.now() )
            )
        }
    }

    @Test
    fun initializing_DataStreamPoint_with_empty_triggerIds_fails()
    {
        assertFailsWith<IllegalArgumentException> {
            DataStreamPoint(
                0,
                UUID.randomUUID(),
                "Device",
                measurement( StubData(), 0 ),
                emptyList(),
                SyncPoint( Clock.System.now() )
            )
        }
    }

    @Test
    fun synchronizeToUTC_succeeds()
    {
        val doubleSpeed = createDoubleSpeedSyncPoint()
        val point = DataStreamPoint(
            0,
            UUID.randomUUID(),
            "Test device",
            measurement( StubData(), 1000, null ),
            listOf( 0 ),
            doubleSpeed
        )

        val synchronized = point.synchronizeToUTC()
        assertEquals( 500, synchronized.measurement.sensorStartTime )
        assertEquals( SyncPoint.UTC, synchronized.syncPoint )
    }
}
