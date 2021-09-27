@file:Suppress( "ParameterListWrapping" )

package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.test.Mock
import kotlinx.datetime.Clock

// TODO: Due to a bug, `Service` cannot be used here, although that would be preferred.
//       Change this once this is fixed: https://youtrack.jetbrains.com/issue/KT-24700
// private typealias Service = RecruitmentService


class RecruitmentServiceMock(
    private val addParticipantResult: Participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) ),
    private val getParticipantResult: Participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) ),
    private val getParticipantsResult: List<Participant> = emptyList(),
    private val deployParticipantResult: ParticipantGroupStatus = groupStatus,
    private val getParticipantGroupStatusListResult: List<ParticipantGroupStatus> = emptyList(),
    private val stopParticipantGroupResult: ParticipantGroupStatus = groupStatus
) : Mock<RecruitmentService>(), RecruitmentService
{
    companion object
    {
        private val now = Clock.System.now()
        private val groupStatus = ParticipantGroupStatus(
            StudyDeploymentStatus.Invited( now, UUID.randomUUID(), emptyList(), null ),
            now,
            emptySet() )
    }


    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ) =
        addParticipantResult
        .also { trackSuspendCall( RecruitmentService::addParticipant, studyId, email ) }

    override suspend fun getParticipant( studyId: UUID, participantId: UUID ) =
        getParticipantResult
        .also { trackSuspendCall( RecruitmentService::getParticipant, studyId, participantId ) }

    override suspend fun getParticipants( studyId: UUID ) =
        getParticipantsResult
        .also { trackSuspendCall( RecruitmentService::getParticipants, studyId ) }

    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ) =
        deployParticipantResult
        .also { trackSuspendCall( RecruitmentService::deployParticipantGroup, studyId, group ) }

    override suspend fun getParticipantGroupStatusList( studyId: UUID ) =
        getParticipantGroupStatusListResult
        .also { trackSuspendCall( RecruitmentService::getParticipantGroupStatusList, studyId ) }

    override suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ) =
        stopParticipantGroupResult
        .also { trackSuspendCall( RecruitmentService::stopParticipantGroup, studyId, groupId ) }
}
