package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


/**
 * Serializable application service requests to [DataStreamServiceRequest] which can be executed on demand.
 */
@Serializable
sealed class DataStreamServiceRequest<out TReturn> : ApplicationServiceRequest<DataStreamService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = DataStreamService.API_VERSION

    object Serializer : KSerializer<DataStreamServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class OpenDataStreams( val configuration: DataStreamsConfiguration ) : DataStreamServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
        override suspend fun invokeOn( service: DataStreamService ) = service.openDataStreams( configuration )
    }

    @Serializable
    data class AppendToDataStreams(
        val studyDeploymentId: UUID,
        @Serializable( DataStreamBatchSerializer::class )
        val batch: DataStreamBatch
    ) : DataStreamServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
        override suspend fun invokeOn( service: DataStreamService ) =
            service.appendToDataStreams( studyDeploymentId, batch )
    }

    @Serializable
    data class GetDataStream(
        val dataStream: DataStreamId,
        val fromSequenceId: Long,
        val toSequenceIdInclusive: Long? = null
    ) : DataStreamServiceRequest<DataStreamBatch>()
    {
        override fun getResponseSerializer() = DataStreamBatchSerializer
        override suspend fun invokeOn( service: DataStreamService ) =
            service.getDataStream( dataStream, fromSequenceId, toSequenceIdInclusive )
    }

    @Serializable
    data class CloseDataStreams( val studyDeploymentIds: Set<UUID> ) :
        DataStreamServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
        override suspend fun invokeOn( service: DataStreamService ) = service.closeDataStreams( studyDeploymentIds )
    }

    @Serializable
    data class RemoveDataStreams( val studyDeploymentIds: Set<UUID> ) :
        DataStreamServiceRequest<Boolean>()
    {
        override fun getResponseSerializer() = serializer<Boolean>()
        override suspend fun invokeOn( service: DataStreamService ) = service.removeDataStreams( studyDeploymentIds )
    }
}
