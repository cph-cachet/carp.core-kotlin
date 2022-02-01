package dk.cachet.carp.studies.application

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [RecruitmentServiceHost].
 */
class RecruitmentServiceHostTest : RecruitmentServiceTest
{
    companion object
    {
        fun createService(): Pair<RecruitmentService, StudyService>
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
                InMemoryDataStreamService(),
                eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

            val participantService = RecruitmentServiceHost(
                InMemoryParticipantRepository(),
                deploymentService,
                eventBus.createApplicationServiceAdapter( RecruitmentService::class ) )

            return Pair( participantService, studyService )
        }
    }

    override fun createService(): Pair<RecruitmentService, StudyService> = RecruitmentServiceHostTest.createService()
}
