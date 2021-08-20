package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.test.Mock


class DataStreamServiceMock(
    val getDataStreamResult: DataStreamBatch = MutableDataStreamBatch()
) : Mock<DataStreamService>(), DataStreamService
{
    override suspend fun openDataStreams( configuration: DataStreamsConfiguration ) =
        trackSuspendCall( DataStreamService::openDataStreams, configuration )

    override suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch ) =
        trackSuspendCall( DataStreamService::appendToDataStreams, studyDeploymentId, batch )

    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ): DataStreamBatch
    {
        trackSuspendCall( DataStreamService::getDataStream, dataStream, fromSequenceId, toSequenceIdInclusive )
        return getDataStreamResult
    }

    override suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> ) =
        trackSuspendCall( DataStreamService::closeDataStreams, studyDeploymentIds )
}
