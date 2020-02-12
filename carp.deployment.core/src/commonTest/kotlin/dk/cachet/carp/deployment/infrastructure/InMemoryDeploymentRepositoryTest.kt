package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.DeploymentRepositoryTest


/**
 * Tests whether [InMemoryDeploymentRepository] is implemented correctly.
 */
class InMemoryDeploymentRepositoryTest : DeploymentRepositoryTest
{
    override fun createRepository(): DeploymentRepository = InMemoryDeploymentRepository()
}
