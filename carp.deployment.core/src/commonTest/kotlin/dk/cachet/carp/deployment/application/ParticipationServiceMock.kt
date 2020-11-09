package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.test.Mock

// TODO: Due to a bug, `Service` cannot be used here, although that would be preferred.
//       Change this once this is fixed: https://youtrack.jetbrains.com/issue/KT-24700
// private typealias Service = ParticipationService


class ParticipationServiceMock(
    private val getActiveParticipationInvitationResult: Set<ActiveParticipationInvitation> = emptySet()
) : Mock<ParticipationService>(), ParticipationService
{
    override suspend fun addParticipation( studyDeploymentId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ) =
        Participation( studyDeploymentId )
        .also { trackSuspendCall( ParticipationService::addParticipation, studyDeploymentId, deviceRoleNames, identity, invitation ) }

    override suspend fun getActiveParticipationInvitations( accountId: UUID ) =
        getActiveParticipationInvitationResult
        .also { trackSuspendCall( ParticipationService::getActiveParticipationInvitations, accountId ) }
}
