package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.services.createServiceInvoker
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [DataStreamServiceRequest] which can be executed on demand.
 */
@Serializable
sealed class DataStreamServiceRequest
{
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
}
