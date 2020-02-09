package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.test.Mock


class UserServiceMock(
    private val addParticipationResult: Participation = Participation( UUID.randomUUID() ),
    private val getParticipationsForStudyDeploymentResult: List<Participation> = listOf()
) : Mock<UserService>(), UserService
{
    override suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity ): Participation
    {
        trackSuspendCall( UserService::addParticipation, studyDeploymentId, identity )
        return addParticipationResult
    }

    override suspend fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
    {
        trackSuspendCall( UserService::getParticipationsForStudyDeployment, studyDeploymentId )
        return getParticipationsForStudyDeploymentResult
    }
}
