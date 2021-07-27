package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [DataStreamSequence].
 */
class DataStreamSequenceTest
{
    private val stubDataStream = dataStreamId<StubData>( UUID.randomUUID(), "Device" )
    private val stubSyncPoint = SyncPoint( Clock.System.now() )

    @Test
    fun initialization_with_negative_firstSequenceId_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamSequence( stubDataStream, -1, listOf( 1 ), stubSyncPoint )
        }
    }

    @Test
    fun initialization_with_empty_triggerIds_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            DataStreamSequence( stubDataStream, 0, emptyList(), stubSyncPoint )
        }
    }

    @Test
    fun appendMeasurements_succeeds()
    {
        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements(
            measurement( StubData(), 0 ),
            measurement( StubData(), 10 )
        )

        assertEquals( 0L..1, sequence.range )
    }

    @Test
    fun appendMeasurements_fails_for_incorrect_data_type()
    {
        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )

        assertFailsWith<IllegalArgumentException> { sequence.appendMeasurements( measurement( StubDataPoint(), 0 ) ) }
    }

    @Test
    fun appendMeasurement_fails_when_list_contains_incorrect_data_type()
    {
        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )

        assertFailsWith<IllegalArgumentException>
        {
            sequence.appendMeasurements(
                measurement( StubData(), 0 ),
                measurement( StubDataPoint(), 0 ) // Incorrect for `stubDataStream`.
            )
        }
    }

    @Test
    fun appendSequence_succeeds()
    {
        val measurement = measurement( StubData(), 0 )

        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements( measurement )

        val toAppend = DataStreamSequence( stubDataStream, 1, listOf( 1 ), stubSyncPoint )
        toAppend.appendMeasurements( measurement )

        sequence.appendSequence( toAppend )
        assertEquals( 0L..1, sequence.range )
    }

    @Test
    fun canAppendSequence_is_false_for_non_matching_data_stream()
    {
        val measurement = measurement( StubData(), 0 )

        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements( measurement )

        val wrongTrigger = DataStreamSequence( stubDataStream, 1, listOf( 0 ), stubSyncPoint )
        wrongTrigger.appendMeasurements( measurement )
        assertFalse( sequence.canAppendSequence( wrongTrigger ) )
    }

    @Test
    fun canAppendSequence_is_false_when_there_is_a_gap()
    {
        val measurement = measurement( StubData(), 0 )

        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements( measurement )

        val doesNotFollow = DataStreamSequence( stubDataStream, 2, listOf( 1 ), stubSyncPoint )
        doesNotFollow.appendMeasurements( measurement )
        assertFalse( sequence.canAppendSequence( doesNotFollow ) )
    }

    @Test
    fun canAppendSequence_to_empty_sequence_succeeds()
    {
        val measurement = measurement( StubData(), 0 )

        val sequence = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )

        val firstItem = DataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        firstItem.appendMeasurements( measurement )
        assertTrue( sequence.canAppendSequence( firstItem ) )
    }

    @Test
    fun getDataStreamPoints_succeeds()
    {
        val deploymentId = UUID.randomUUID()
        val device = "Device"

        val measurement1 = measurement( StubData(), 0 )
        val measurement2 = measurement( StubData(), 1 )

        val triggerIds = listOf( 1 )

        val sequence = DataStreamSequence(
            dataStreamId<StubData>( deploymentId, device ),
            0,
            triggerIds,
            stubSyncPoint
        )
        sequence.appendMeasurements( measurement1, measurement2 )

        val expectedPoints = listOf(
            DataStreamPoint( 0, deploymentId, device, measurement1, triggerIds, stubSyncPoint ),
            DataStreamPoint( 1, deploymentId, device, measurement2, triggerIds, stubSyncPoint )
        )
        assertEquals( expectedPoints, sequence.getDataStreamPoints() )
    }
}
