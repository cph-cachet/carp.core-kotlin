package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration


/**
 * A proxy for a data stream [service] which notifies of incoming requests, responses, and events through [log]
 * and keeps a history of requests in [loggedRequests] and published events in [loggedEvents].
 */
class DataStreamServiceLoggingProxy(
    service: DataStreamService,
    eventBus: EventBus,
    log: (LoggedRequest<DataStreamService, DataStreamService.Event>) -> Unit = { }
) :
    ApplicationServiceLoggingProxy<DataStreamService, DataStreamService.Event>(
        service,
        DataStreamService::class,
        DataStreamService.Event::class,
        eventBus,
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
