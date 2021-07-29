package dk.cachet.carp.data.domain

import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.MutableDataStreamBatch


/**
 * A repository which holds data points of data streams of multiple study deployments.
 */
interface DataStreamRepository
{
    /**
     * Append all data stream sequences contained in [batch] to this repository.
     *
     * @throws IllegalArgumentException when the start of any of the sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    suspend fun appendToDataStreams( batch: DataStreamBatch )

    /**
     * Append [sequence] to a non-existing or previously appended data stream in this repository.
     *
     * @throws IllegalArgumentException when the start of the [sequence] range precedes the end of
     *   a previously appended sequence to the same data stream.
     */
    suspend fun appendToDataStream( sequence: DataStreamSequence ) =
        appendToDataStreams( MutableDataStreamBatch().apply { appendSequence( sequence ) } )

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive range
     * defined by [fromSequenceId] and [toSequenceIdInclusive].
     * If [toSequenceIdInclusive] is null, all data points starting [fromSequenceId] are returned.
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     */
    suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long? = null
    ): DataStreamBatch

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive [range].
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     */
    suspend fun getDataStream( dataStream: DataStreamId, range: LongRange ): DataStreamBatch =
        getDataStream( dataStream, range.first, range.last )
}
