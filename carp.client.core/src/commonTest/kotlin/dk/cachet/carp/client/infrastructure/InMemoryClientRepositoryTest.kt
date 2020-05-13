package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.ClientRepository
import dk.cachet.carp.client.domain.ClientRepositoryTest
import dk.cachet.carp.deployment.application.DeploymentService


/**
 * Tests whether [InMemoryClientRepository] is implemented correctly.
 */
class InMemoryClientRepositoryTest : ClientRepositoryTest
{
    override fun createRepository( deploymentService: DeploymentService ): ClientRepository
    {
        return InMemoryClientRepository( deploymentService )
    }
}
