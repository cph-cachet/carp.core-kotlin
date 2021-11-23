package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TIME_SPAN_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import kotlin.test.*


/**
 * Tests for [Measurement].
 */
class MeasurementTest
{
    @Test
    fun sensorEndTime_needs_to_lie_after_sensorStartTime()
    {
        // Same end time as start time is allowed.
        Measurement( 0, 0, STUB_DATA_POINT_TYPE, StubDataPoint() )

        assertFailsWith<IllegalArgumentException>
        {
            Measurement( 10, 0, STUB_DATA_POINT_TYPE, StubDataPoint() )
        }
    }

    @Test
    fun getDataTimeType_succeeds()
    {
        val point = Measurement( 0, null, STUB_DATA_POINT_TYPE, StubDataPoint() )
        assertEquals( DataTimeType.POINT, point.getDataTimeType() )

        val timeSpan = Measurement( 0, 1, STUB_DATA_TIME_SPAN_TYPE, StubDataTimeSpan() )
        assertEquals( DataTimeType.TIME_SPAN, timeSpan.getDataTimeType() )
    }

    @Test
    fun synchronizeToUTC_succeeds()
    {
        val point = Measurement( 1000, null, STUB_DATA_POINT_TYPE, StubDataPoint() )

        val doubleSpeed = createDoubleSpeedSyncPoint()
        val syncedPoint = point.synchronizeToUTC( doubleSpeed )
        assertEquals( 500, syncedPoint.sensorStartTime )
        assertNull( syncedPoint.sensorEndTime )
    }

    @Test
    fun synchronizeToUTC_converts_both_start_and_end_time()
    {
        val point = Measurement( 1000, 2000, STUB_DATA_TIME_SPAN_TYPE, StubDataTimeSpan() )

        val doubleSpeed = createDoubleSpeedSyncPoint()
        val syncedPoint = point.synchronizeToUTC( doubleSpeed )
        assertEquals( 500, syncedPoint.sensorStartTime )
        assertEquals( 1000, syncedPoint.sensorEndTime )
    }
}
