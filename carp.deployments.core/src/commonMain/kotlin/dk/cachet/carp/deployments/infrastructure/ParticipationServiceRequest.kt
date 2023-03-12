package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData
import kotlinx.serialization.*


/**
 * Serializable application service requests to [ParticipationService] which can be executed on demand.
 */
@Serializable
sealed class ParticipationServiceRequest<out TReturn> : ApplicationServiceRequest<ParticipationService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = ParticipationService.API_VERSION

    object Serializer : KSerializer<ParticipationServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class GetActiveParticipationInvitations( val accountId: UUID ) :
        ParticipationServiceRequest<Set<ActiveParticipationInvitation>>()
    {
        override fun getResponseSerializer() = serializer<Set<ActiveParticipationInvitation>>()
    }

    @Serializable
    data class GetParticipantData( val studyDeploymentId: UUID ) : ParticipationServiceRequest<ParticipantData>()
    {
        override fun getResponseSerializer() = serializer<ParticipantData>()
    }

    @Serializable
    data class GetParticipantDataList( val studyDeploymentIds: Set<UUID> ) :
        ParticipationServiceRequest<List<ParticipantData>>()
    {
        override fun getResponseSerializer() = serializer<List<ParticipantData>>()
    }

    @Serializable
    data class SetParticipantData(
        val studyDeploymentId: UUID,
        val data: Map<InputDataType, Data?>,
        val inputByParticipantRole: String? = null
    ) :
        ParticipationServiceRequest<ParticipantData>()
    {
        override fun getResponseSerializer() = serializer<ParticipantData>()
    }
}
