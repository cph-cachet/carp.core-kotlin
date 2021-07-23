package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [DataStreamSequence].
 */
class DataStreamSequenceTest
{
    private val stubDataStream = dataStreamId<StubData>( UUID.randomUUID(), "Device" )
    private val stubSyncPoint = SyncPoint( Clock.System.now() )

    @Test
    fun initialization_succeeds()
    {
        val sequence = DataStreamSequence.fromMeasurements(
            stubDataStream,
            0,
            listOf( measurement( StubData(), 0, ), measurement( StubData(), 10 ) ),
            listOf( 1 ),
            stubSyncPoint
        )

        assertEquals( 0L..1, sequence.range )
    }

    @Test
    fun initialization_with_negative_firstSequenceId_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamSequence.fromMeasurements(
                stubDataStream,
                -1,
                listOf( measurement( StubData(), 0 ) ),
                listOf( 1 ),
                stubSyncPoint
            )
        }
    }

    @Test
    fun initialization_with_empty_measurements_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamSequence.fromMeasurements<Data>(
                stubDataStream,
                0,
                emptyList(),
                listOf( 1 ),
                stubSyncPoint
            )
        }
    }

    @Test
    fun initialization__with_measurements_of_differing_data_types_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamSequence.fromMeasurements(
                stubDataStream,
                0,
                listOf(
                    measurement( StubData(), 0 ),
                    measurement( StubDataPoint(), 0 )
                ),
                listOf( 1 ),
                stubSyncPoint
            )
        }
    }

    @Test
    fun initialization__with_empty_triggerIds_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamSequence.fromMeasurements<Data>(
                stubDataStream,
                0,
                listOf( measurement( StubData(), 0 ) ),
                emptyList(),
                stubSyncPoint
            )
        }
    }

    @Test
    fun getDataStreamPoints_succeeds()
    {
        val deploymentId = UUID.randomUUID()
        val device = "Device"
        val dataStream = dataStreamId<StubData>( deploymentId, device )

        val measurement1 = measurement( StubData(), 0 )
        val measurement2 = measurement( StubData(), 1 )

        val triggerIds = listOf( 1 )

        val batch = DataStreamSequence.fromMeasurements(
            dataStream,
            0,
            listOf( measurement1, measurement2 ),
            triggerIds,
            stubSyncPoint
        )

        val expectedPoints = listOf(
            DataStreamPoint( 0, deploymentId, device, measurement1, triggerIds, stubSyncPoint ),
            DataStreamPoint( 1, deploymentId, device, measurement2, triggerIds, stubSyncPoint )
        )
        assertEquals( expectedPoints, batch.getDataStreamPoints() )
    }
}
