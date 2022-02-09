package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration


/**
 * A proxy for a data stream [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests].
 */
class DataStreamServiceLog(
    service: DataStreamService,
    log: (LoggedRequest<DataStreamService, DataStreamService.Event>) -> Unit = { }
) :
    ApplicationServiceLog<DataStreamService, DataStreamService.Event>(
        service,
        DataStreamService::class,
        DataStreamService.Event::class,
        SingleThreadedEventBus(),
        log
    ),
    DataStreamService
{
    override suspend fun openDataStreams( configuration: DataStreamsConfiguration ) =
        log( DataStreamServiceRequest.OpenDataStreams( configuration ) )

    override suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch ) =
        log( DataStreamServiceRequest.AppendToDataStreams( studyDeploymentId, batch ) )

    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ): DataStreamBatch =
        log( DataStreamServiceRequest.GetDataStream( dataStream, fromSequenceId, toSequenceIdInclusive ) )

    override suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> ) =
        log( DataStreamServiceRequest.CloseDataStreams( studyDeploymentIds ) )

    override suspend fun removeDataStreams( studyDeploymentIds: Set<UUID> ): Boolean =
        log( DataStreamServiceRequest.RemoveDataStreams( studyDeploymentIds ) )
}
