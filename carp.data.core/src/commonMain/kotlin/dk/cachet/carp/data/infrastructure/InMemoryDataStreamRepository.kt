package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.intersect
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.domain.DataStreamRepository


/**
 * A [DataStreamRepository] which holds data points in memory as long as the instance is held in memory.
 */
class InMemoryDataStreamRepository : DataStreamRepository
{
    private val dataStreams: MutableDataStreamBatch = MutableDataStreamBatch()


    /**
     * Append all data stream sequences contained in [batch] to this repository.
     *
     * @throws IllegalArgumentException when the start of any of the sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    override suspend fun appendToDataStreams( batch: DataStreamBatch ) =
        dataStreams.appendBatch( batch )

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive range
     * defined by [fromSequenceId] and [toSequenceIdInclusive].
     * If [toSequenceIdInclusive] is null, all data points starting [fromSequenceId] are returned.
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     */
    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ): DataStreamBatch = dataStreams.sequences
        .filter { it.dataStream == dataStream }
        .mapNotNull {
            val queryRange = fromSequenceId.rangeTo( toSequenceIdInclusive ?: Long.MAX_VALUE )
            val subRange = it.range.intersect( queryRange )

            if ( subRange.isEmpty() ) null
            else MutableDataStreamSequence( dataStream, subRange.first, it.triggerIds, it.syncPoint )
                .apply {
                    val startOffset = subRange.first - it.range.first
                    val exclusiveEnd = startOffset + subRange.last - subRange.first + 1
                    check( startOffset <= Int.MAX_VALUE && exclusiveEnd <= Int.MAX_VALUE )
                        { "Exceeded capacity of measurements which can be held in memory." }
                    appendMeasurements( it.measurements.subList( startOffset.toInt(), exclusiveEnd.toInt() ) )
                }
        }
        .fold( MutableDataStreamBatch() ) { batch, sequence ->
            batch.apply { appendSequence( sequence ) }
        }
}
