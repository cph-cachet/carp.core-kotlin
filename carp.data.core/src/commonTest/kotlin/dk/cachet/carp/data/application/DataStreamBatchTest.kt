package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [DataStreamBatch].
 */
class DataStreamBatchTest
{
    @Test
    fun initializing_DataStreamBatch_with_negative_firstSequenceId_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamBatch(
                -1,
                listOf( measurement( StubData(), 0 ) ),
                listOf( 1 ),
                SyncPoint( Clock.System.now() )
            )
        }
    }

    @Test
    fun initializing_DataStreamBatch_with_empty_measurements_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamBatch<Data>(
                0,
                emptyList(),
                listOf( 1 ),
                SyncPoint( Clock.System.now() )
            )
        }
    }

    @Test
    fun initializing_DataStreamBatch_with_measurements_of_differing_data_types_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamBatch(
                0,
                listOf(
                    measurement( StubData(), 0 ),
                    measurement( StubDataPoint(), 0 )
                ),
                listOf( 1 ),
                SyncPoint( Clock.System.now() )
            )
        }
    }

    @Test
    fun initializing_DataStreamBatch_with_empty_triggerIds_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamBatch<Data>(
                0,
                listOf( measurement( StubData(), 0 ) ),
                emptyList(),
                SyncPoint( Clock.System.now() )
            )
        }
    }

    @Test
    fun getDataStreamPoints_succeeds()
    {
        val measurement1 = measurement( StubData(), 0 )
        val measurement2 = measurement( StubData(), 1 )
        val triggerIds = listOf( 1 )
        val syncPoint = SyncPoint( Clock.System.now() )
        val batch = DataStreamBatch(
            0,
            listOf( measurement1, measurement2 ),
            triggerIds,
            syncPoint
        )
        val deploymentId = UUID.randomUUID()
        val device = "Device"
        val dataStream = DataStreamId( deploymentId, device, STUB_DATA_TYPE )

        val expectedPoints = listOf(
            DataStreamPoint( 0, deploymentId, device, measurement1, triggerIds, syncPoint ),
            DataStreamPoint( 1, deploymentId, device, measurement2, triggerIds, syncPoint )
        )
        assertEquals( expectedPoints, batch.getDataStreamPoints( dataStream ) )
    }

    @Test
    fun getDataStreamPoints_fails_for_non_matching_dataStream_data_type()
    {
        val batch = DataStreamBatch(
            0,
            listOf( measurement( StubData(), 0 ) ),
            listOf( 1 ),
            SyncPoint( Clock.System.now() )
        )
        val dataStream = DataStreamId( UUID.randomUUID(), "Device", STUB_DATA_POINT_TYPE )

        assertFailsWith<IllegalArgumentException> { batch.getDataStreamPoints( dataStream ) }
    }
}
