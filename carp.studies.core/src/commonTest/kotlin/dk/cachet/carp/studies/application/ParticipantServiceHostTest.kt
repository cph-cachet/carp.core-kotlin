package dk.cachet.carp.studies.application

import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
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

        // Create dependent deployment service
        val accountService = InMemoryAccountService()
        val deploymentService = DeploymentServiceHost( InMemoryDeploymentRepository(), accountService )

        val participantService = ParticipantServiceHost( studyRepo, InMemoryParticipantRepository(), deploymentService )

        return Pair( participantService, studyService )
    }
}
