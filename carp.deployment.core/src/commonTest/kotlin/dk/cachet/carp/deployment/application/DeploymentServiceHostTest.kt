package dk.cachet.carp.deployment.application

import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository


/**
 * Tests for [DeploymentServiceHost].
 */
class DeploymentServiceHostTest : DeploymentServiceTest()
{
    override fun createService(): Pair<DeploymentService, AccountService>
    {
        val repository = InMemoryDeploymentRepository()
        val accountService = InMemoryAccountService()
        return Pair( DeploymentServiceHost( repository, accountService ), accountService )
    }
}
