package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.serialization.*


/**
 * Serializable application service requests to [RecruitmentService] which can be executed on demand.
 */
@Serializable
sealed class RecruitmentServiceRequest<out TReturn> : ApplicationServiceRequest<RecruitmentService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = RecruitmentService.API_VERSION

    object Serializer : KSerializer<RecruitmentServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class AddParticipant( val studyId: UUID, val email: EmailAddress ) : RecruitmentServiceRequest<Participant>()
    {
        override fun getResponseSerializer() = serializer<Participant>()
        override suspend fun invokeOn( service: RecruitmentService ) = service.addParticipant( studyId, email )
    }

    @Serializable
    data class GetParticipant( val studyId: UUID, val participantId: UUID ) : RecruitmentServiceRequest<Participant>()
    {
        override fun getResponseSerializer() = serializer<Participant>()
        override suspend fun invokeOn( service: RecruitmentService ) = service.getParticipant( studyId, participantId )
    }

    @Serializable
    data class GetParticipants( val studyId: UUID ) : RecruitmentServiceRequest<List<Participant>>()
    {
        override fun getResponseSerializer() = serializer<List<Participant>>()
        override suspend fun invokeOn( service: RecruitmentService ) = service.getParticipants( studyId )
    }

    @Serializable
    data class InviteNewParticipantGroup( val studyId: UUID, val group: Set<AssignedParticipantRoles> ) :
        RecruitmentServiceRequest<ParticipantGroupStatus>()
    {
        override fun getResponseSerializer() = serializer<ParticipantGroupStatus>()
        override suspend fun invokeOn( service: RecruitmentService ) =
            service.inviteNewParticipantGroup( studyId, group )
    }

    @Serializable
    data class GetParticipantGroupStatusList( val studyId: UUID ) :
        RecruitmentServiceRequest<List<ParticipantGroupStatus>>()
    {
        override fun getResponseSerializer() = serializer<List<ParticipantGroupStatus>>()
        override suspend fun invokeOn( service: RecruitmentService ) = service.getParticipantGroupStatusList( studyId )
    }

    @Serializable
    data class StopParticipantGroup( val studyId: UUID, val groupId: UUID ) :
        RecruitmentServiceRequest<ParticipantGroupStatus>()
    {
        override fun getResponseSerializer() = serializer<ParticipantGroupStatus>()
        override suspend fun invokeOn( service: RecruitmentService ) = service.stopParticipantGroup( studyId, groupId )
    }
}
