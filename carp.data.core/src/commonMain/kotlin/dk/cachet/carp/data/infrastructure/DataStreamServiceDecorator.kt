package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration


class DataStreamServiceDecorator(
    service: DataStreamService,
    requestDecorator: (Command<DataStreamServiceRequest<*>>) -> Command<DataStreamServiceRequest<*>>
) : ApplicationServiceDecorator<DataStreamService, DataStreamServiceRequest<*>>( service, requestDecorator ),
    DataStreamService
{
    override suspend fun openDataStreams( configuration: DataStreamsConfiguration ): Unit = invoke(
        DataStreamServiceRequest.OpenDataStreams( configuration )
    )

    override suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch ): Unit = invoke(
        DataStreamServiceRequest.AppendToDataStreams( studyDeploymentId, batch )
    )

    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ): DataStreamBatch = invoke(
        DataStreamServiceRequest.GetDataStream( dataStream, fromSequenceId, toSequenceIdInclusive )
    )

    override suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> ): Unit = invoke(
        DataStreamServiceRequest.CloseDataStreams( studyDeploymentIds )
    )

    override suspend fun removeDataStreams( studyDeploymentIds: Set<UUID> ): Set<UUID> = invoke(
        DataStreamServiceRequest.RemoveDataStreams( studyDeploymentIds )
    )

    override suspend fun DataStreamServiceRequest<*>.invokeOnService( service: DataStreamService ): Any =
        when ( this )
        {
            is DataStreamServiceRequest.OpenDataStreams -> service.openDataStreams( configuration )
            is DataStreamServiceRequest.AppendToDataStreams -> service.appendToDataStreams( studyDeploymentId, batch )
            is DataStreamServiceRequest.GetDataStream ->
                service.getDataStream( dataStream, fromSequenceId, toSequenceIdInclusive )
            is DataStreamServiceRequest.CloseDataStreams -> service.closeDataStreams( studyDeploymentIds )
            is DataStreamServiceRequest.RemoveDataStreams -> service.removeDataStreams( studyDeploymentIds )
        }
}
