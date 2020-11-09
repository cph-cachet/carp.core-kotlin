package dk.cachet.carp.studies.application

import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.application.ParticipationServiceHost
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployment.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [ParticipantServiceHost].
 */
class ParticipantServiceHostTest : ParticipantServiceTest
{
    override fun createService(): Pair<ParticipantService, StudyService>
    {
        // Create dependent study service.
        val studyRepo = InMemoryStudyRepository()
        val studyService = StudyServiceHost( studyRepo )

        // Create dependent deployment service.
        val deploymentRepo = InMemoryDeploymentRepository()
        val deploymentService = DeploymentServiceHost( deploymentRepo )

        // Create dependent participation service.
        val accountService = InMemoryAccountService()
        val participationRepository = InMemoryParticipationRepository()
        val participationService = ParticipationServiceHost( deploymentRepo, participationRepository, accountService )

        val participantService = ParticipantServiceHost(
            studyRepo,
            InMemoryParticipantRepository(),
            deploymentService,
            participationService )

        return Pair( participantService, studyService )
    }
}
