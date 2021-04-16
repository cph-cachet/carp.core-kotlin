package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.deployments.domain.DeploymentRepository
import dk.cachet.carp.deployments.domain.DeploymentRepositoryTest


/**
 * Tests whether [InMemoryDeploymentRepository] is implemented correctly.
 */
class InMemoryDeploymentRepositoryTest : DeploymentRepositoryTest
{
    override fun createRepository(): DeploymentRepository = InMemoryDeploymentRepository()
}
