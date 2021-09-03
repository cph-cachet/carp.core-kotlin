package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlin.test.*


private val stubDataStream = dataStreamId<StubData>( UUID.randomUUID(), "Device" )


/**
 * Tests for implementations of [DataStreamSequence].
 */
interface DataStreamSequenceTest
{
    fun createStubDataStreamSequence(
        firstSequenceId: Long = 0,
        measurements: List<Measurement<*>>,
        triggerIds: List<Int> = listOf( 1 ),
        syncPoint: SyncPoint = stubSyncPoint,
        dataStream: DataStreamId = stubDataStream
    ): DataStreamSequence


    @Test
    fun isImmediatelyFollowedBy_is_false_for_non_matching_data_stream()
    {
        val measurement = measurement( StubData(), 0 )
        val sequence = createStubDataStreamSequence( 0, listOf( measurement ), triggerIds = listOf( 0 ) )
        val wrongTrigger = createStubDataStreamSequence( 1, listOf( measurement ), triggerIds = listOf( 1 ) )

        assertFalse( sequence.isImmediatelyFollowedBy( wrongTrigger ) )
    }

    @Test
    fun isImmediatelyFollowedBy_is_false_when_there_is_a_gap()
    {
        val measurement = measurement( StubData(), 0 )
        val sequence = createStubDataStreamSequence(0, listOf( measurement ) )
        val doesNotFollow = createStubDataStreamSequence( 2, listOf( measurement ) )

        assertFalse( sequence.isImmediatelyFollowedBy( doesNotFollow ) )
    }

    @Test
    fun isImmediatelyFollowedBy_is_true_for_empty_sequence()
    {
        val measurement = measurement( StubData(), 0 )
        val emptySequence = createStubDataStreamSequence( 0, emptyList() )
        val firstItem = createStubDataStreamSequence( 0, listOf( measurement ) )

        assertTrue( emptySequence.isImmediatelyFollowedBy( firstItem ) )
    }

    @Test
    fun getDataStreamPoints_succeeds()
    {
        val deploymentId = UUID.randomUUID()
        val device = "Device"

        val measurement1 = measurement( StubData(), 0 )
        val measurement2 = measurement( StubData(), 1 )

        val triggerIds = listOf( 1 )

        val sequence = createStubDataStreamSequence(
            0,
            listOf( measurement1, measurement2 ),
            dataStream = dataStreamId<StubData>( deploymentId, device ),
        )

        val expectedPoints = listOf(
            DataStreamPoint( 0, deploymentId, device, measurement1, triggerIds, stubSyncPoint ),
            DataStreamPoint( 1, deploymentId, device, measurement2, triggerIds, stubSyncPoint )
        )
        assertEquals( expectedPoints, sequence.toList() )
    }
}


/**
 * Tests for [MutableDataStreamSequence].
 */
class MutableDataStreamSequenceTest : DataStreamSequenceTest
{
    override fun createStubDataStreamSequence(
        firstSequenceId: Long,
        measurements: List<Measurement<*>>,
        triggerIds: List<Int>,
        syncPoint: SyncPoint,
        dataStream: DataStreamId
    ): DataStreamSequence
    {
        val sequence = MutableDataStreamSequence( dataStream, firstSequenceId, triggerIds, syncPoint )
        sequence.appendMeasurements( measurements )

        return sequence
    }


    @Test
    fun initialization_with_negative_firstSequenceId_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            MutableDataStreamSequence( stubDataStream, -1, listOf( 1 ), stubSyncPoint )
        }
    }

    @Test
    fun initialization_with_empty_triggerIds_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            MutableDataStreamSequence( stubDataStream, 0, emptyList(), stubSyncPoint )
        }
    }

    @Test
    fun appendMeasurements_succeeds()
    {
        val sequence = MutableDataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements(
            measurement( StubData(), 0 ),
            measurement( StubData(), 10 )
        )

        assertEquals( 0L..1, sequence.range )
    }

    @Test
    fun appendMeasurements_fails_for_incorrect_data_type()
    {
        val sequence = MutableDataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )

        assertFailsWith<IllegalArgumentException> { sequence.appendMeasurements( measurement( StubDataPoint(), 0 ) ) }
    }

    @Test
    fun appendMeasurement_fails_when_list_contains_incorrect_data_type()
    {
        val sequence = MutableDataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )

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

        val sequence = MutableDataStreamSequence( stubDataStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements( measurement )

        val toAppend = MutableDataStreamSequence( stubDataStream, 1, listOf( 1 ), stubSyncPoint )
        toAppend.appendMeasurements( measurement )

        sequence.appendSequence( toAppend )
        assertEquals( 0L..1, sequence.range )
    }
}
