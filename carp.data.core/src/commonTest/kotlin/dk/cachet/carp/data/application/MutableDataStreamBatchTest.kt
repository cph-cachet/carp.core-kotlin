package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlin.test.*


/**
 * Tests for [MutableDataStreamBatch].
 */
class MutableDataStreamBatchTest
{
    private val stubData = StubData()

    @Test
    fun appendSequence_succeeds_with_no_prior_sequences()
    {
        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence( 0, stubData )

        batch.appendSequence( sequence )

        assertEquals( 1, batch.sequences.count() )
    }

    @Test
    fun appendSequence_succeeds_with_prior_sequence_with_gap()
    {
        val batch = MutableDataStreamBatch()
        batch.appendSequence( createStubSequence( 0, stubData ) )

        val gapSequence = createStubSequence( 2, stubData )
        batch.appendSequence( gapSequence )

        assertEquals( 2, batch.sequences.count() )
    }

    @Test
    fun appendSequence_succeeds_with_new_triggerIds()
    {
        val batch = MutableDataStreamBatch()

        val dataStream = dataStreamId<StubData>( UUID.randomUUID(), "Device" )
        val measurement = measurement( StubData(), 0 )
        val triggerId1Sequence = MutableDataStreamSequence(
            dataStream,
            0,
            listOf( 1 ),
            stubSyncPoint
        )
        triggerId1Sequence.appendMeasurements( measurement )
        val triggerId2Sequence = MutableDataStreamSequence(
            dataStream,
            1,
            listOf( 2 ),
            stubSyncPoint
        )
        triggerId2Sequence.appendMeasurements( measurement )
        batch.appendSequence( triggerId1Sequence )
        batch.appendSequence( triggerId2Sequence )

        assertEquals( 2, batch.sequences.count() ) // Due to the different triggerIds, the sequence is not merged.
    }

    @Test
    fun appendSequence_merges_sequence_when_there_is_no_sequence_gap()
    {
        val batch = MutableDataStreamBatch()
        batch.appendSequence( createStubSequence( 0, stubData ) )

        val noGapSequence = createStubSequence( 1, stubData )
        batch.appendSequence( noGapSequence )

        val singleSequence = batch.sequences.singleOrNull()
        assertNotNull( singleSequence )
        assertEquals( 0L..1, singleSequence.range )
    }

    @Test
    fun appendSequence_succeeds_for_differing_data_streams()
    {
        val batch = MutableDataStreamBatch()

        batch.appendSequence( createStubSequence( 0, stubData ) )
        batch.appendSequence( createStubSequence( 0, StubDataPoint() ) )

        assertEquals( 2, batch.sequences.count() )
    }

    @Test
    fun appendSequence_fails_for_overlapping_sequence_range()
    {
        val batch = MutableDataStreamBatch()
        batch.appendSequence( createStubSequence( 0, stubData, stubData ) )

        val overlappingSequence = createStubSequence( 1, stubData )
        assertFailsWith<IllegalArgumentException>
        {
            batch.appendSequence( overlappingSequence )
        }
    }
}
