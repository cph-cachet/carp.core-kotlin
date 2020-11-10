package dk.cachet.carp.deployment.application

import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployment.infrastructure.InMemoryParticipationRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest()
{
    override fun createService(): Triple<ParticipationService, DeploymentService, AccountService>
    {
        val deploymentRepository = InMemoryDeploymentRepository()
        val deploymentService = DeploymentServiceHost( deploymentRepository )

        val accountService = InMemoryAccountService()

        val participationRepository = InMemoryParticipationRepository()
        val participationService = ParticipationServiceHost(
            deploymentRepository,
            participationRepository,
            accountService )

        return Triple( participationService, deploymentService, accountService )
    }
}
