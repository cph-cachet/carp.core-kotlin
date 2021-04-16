package dk.cachet.carp.studies.application

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [ParticipantServiceHost].
 */
class ParticipantServiceHostTest : ParticipantServiceTest
{
    override fun createService(): Pair<ParticipantService, StudyService>
    {
        val eventBus = SingleThreadedEventBus()

        // Create dependent study service.
        val studyRepo = InMemoryStudyRepository()
        val studyService = StudyServiceHost(
            studyRepo,
            eventBus.createApplicationServiceAdapter( StudyService::class ) )

        // Create dependent deployment service.
        val deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        // Create dependent participation service.
        val accountService = InMemoryAccountService()
        val participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            accountService,
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )

        val participantService = ParticipantServiceHost(
            InMemoryParticipantRepository(),
            deploymentService,
            participationService,
            eventBus.createApplicationServiceAdapter( ParticipantService::class ) )

        return Pair( participantService, studyService )
    }
}
