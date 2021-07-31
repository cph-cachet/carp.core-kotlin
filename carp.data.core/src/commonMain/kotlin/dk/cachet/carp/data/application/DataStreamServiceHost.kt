package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.domain.DataStreamRepository


/**
 * Store and retrieve [DataStreamPoint]s for study deployments.
 */
class DataStreamServiceHost( private val repository: DataStreamRepository ) : DataStreamService
{
    /**
     * Append a [batch] of data point sequences to corresponding data streams in [studyDeploymentId].
     *
     * @throws IllegalArgumentException when the start of any of the sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    override suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch )
    {
        require( batch.sequences.all { it.dataStream.studyDeploymentId == studyDeploymentId } )
        repository.appendToDataStreams( batch )
    }

    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ): DataStreamBatch = repository.getDataStream( dataStream, fromSequenceId,)
}
