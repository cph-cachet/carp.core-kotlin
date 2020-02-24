package dk.cachet.carp.studies.application

import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import dk.cachet.carp.studies.domain.StudyRepository


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    override fun createService(): Pair<StudyService, StudyRepository>
    {
        val repo = InMemoryStudyRepository()
        val accountService = InMemoryAccountService()
        val deploymentService = DeploymentServiceHost( InMemoryDeploymentRepository(), accountService )
        val service = StudyServiceHost( repo, deploymentService )

        return Pair( service, repo )
    }
}
