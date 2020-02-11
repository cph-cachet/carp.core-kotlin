package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.test.Mock


class ParticipationServiceMock(
    private val addParticipationResult: Participation = Participation( UUID.randomUUID() ),
    private val getParticipationsForStudyDeploymentResult: List<Participation> = listOf()
) : Mock<ParticipationService>(), ParticipationService
{
    override suspend fun addParticipation( studyDeploymentId: UUID, identity: AccountIdentity, invitation: StudyInvitation ): Participation
    {
        trackSuspendCall( ParticipationService::addParticipation, studyDeploymentId, identity, invitation )
        return addParticipationResult
    }

    override suspend fun getParticipationsForStudyDeployment( studyDeploymentId: UUID ): List<Participation>
    {
        trackSuspendCall( ParticipationService::getParticipationsForStudyDeployment, studyDeploymentId )
        return getParticipationsForStudyDeploymentResult
    }
}
