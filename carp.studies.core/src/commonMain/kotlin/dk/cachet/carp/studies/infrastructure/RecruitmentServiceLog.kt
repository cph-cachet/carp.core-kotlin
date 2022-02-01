package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus


/**
 * A proxy for a recruitment [service] which notifies of incoming requests and responses through [log].
 */
class RecruitmentServiceLog( service: RecruitmentService, log: (LoggedRequest<RecruitmentService>) -> Unit ) :
    ApplicationServiceLog<RecruitmentService>( service, log ),
    RecruitmentService
{
    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant =
        log( RecruitmentServiceRequest.AddParticipant( studyId, email ) )

    override suspend fun getParticipant( studyId: UUID, participantId: UUID ): Participant =
        log( RecruitmentServiceRequest.GetParticipant( studyId, participantId ) )

    override suspend fun getParticipants( studyId: UUID ): List<Participant> =
        log( RecruitmentServiceRequest.GetParticipants( studyId ) )

    override suspend fun inviteNewParticipantGroup(
        studyId: UUID,
        group: Set<AssignParticipantDevices>
    ): ParticipantGroupStatus =
        log( RecruitmentServiceRequest.InviteNewParticipantGroup( studyId, group ) )

    override suspend fun getParticipantGroupStatusList( studyId: UUID ): List<ParticipantGroupStatus> =
        log( RecruitmentServiceRequest.GetParticipantGroupStatusList( studyId ) )

    override suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ): ParticipantGroupStatus =
        log( RecruitmentServiceRequest.StopParticipantGroup( studyId, groupId ) )
}
