package dk.cachet.carp.deployment.application

import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository


/**
 * Tests for [DeploymentServiceHost].
 */
class DeploymentServiceHostTest : DeploymentServiceTest()
{
    override fun createService(): DeploymentService = DeploymentServiceHost( InMemoryDeploymentRepository() )
}
