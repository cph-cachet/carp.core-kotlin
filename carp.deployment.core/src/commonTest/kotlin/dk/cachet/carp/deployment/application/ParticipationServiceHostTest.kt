package dk.cachet.carp.deployment.application

import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest()
{
    override fun createService(): Triple<ParticipationService, DeploymentService, AccountService>
    {
        val repository = InMemoryDeploymentRepository()
        val deploymentService = DeploymentServiceHost( repository )
        val accountService = InMemoryAccountService()
        val participationService = ParticipationServiceHost( repository, accountService )

        return Triple( participationService, deploymentService, accountService )
    }
}
