@file:JsExport

package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.services.createServiceInvoker
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Serializable application service requests to [DataStreamServiceRequest] which can be executed on demand.
 */
@Serializable
sealed class DataStreamServiceRequest
{
    @Serializable
    data class OpenDataStreams( val configuration: DataStreamsConfiguration ) :
        DataStreamServiceRequest(),
        ServiceInvoker<DataStreamService, Unit> by createServiceInvoker( DataStreamService::openDataStreams, configuration )

    @Serializable
    data class AppendToDataStreams(
        val studyDeploymentId: UUID,
        @Serializable( DataStreamBatchSerializer::class )
        val batch: DataStreamBatch
    ) : DataStreamServiceRequest(),
        ServiceInvoker<DataStreamService, Unit> by createServiceInvoker( DataStreamService::appendToDataStreams, studyDeploymentId, batch )

    @Serializable
    data class GetDataStream(
        val dataStream: DataStreamId,
        val fromSequenceId: Long,
        val toSequenceIdInclusive: Long? = null
    ) : DataStreamServiceRequest(),
        ServiceInvoker<DataStreamService, DataStreamBatch> by createServiceInvoker( DataStreamService::getDataStream, dataStream, fromSequenceId, toSequenceIdInclusive )

    @Serializable
    data class CloseDataStreams( val studyDeploymentIds: Set<UUID> ) :
        DataStreamServiceRequest(),
        ServiceInvoker<DataStreamService, Unit> by createServiceInvoker( DataStreamService::closeDataStreams, studyDeploymentIds )

    @Serializable
    data class RemoveDataStreams( val studyDeploymentIds: Set<UUID> ) :
        DataStreamServiceRequest(),
        ServiceInvoker<DataStreamService, Boolean> by createServiceInvoker( DataStreamService::removeDataStreams, studyDeploymentIds )
}
