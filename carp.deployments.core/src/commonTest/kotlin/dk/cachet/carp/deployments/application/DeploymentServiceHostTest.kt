package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository


/**
 * Tests for [DeploymentServiceHost].
 */
class DeploymentServiceHostTest : DeploymentServiceTest()
{
    private val eventBus: EventBus = SingleThreadedEventBus()

    override fun createService(): DeploymentService = DeploymentServiceHost(
        InMemoryDeploymentRepository(),
        eventBus.createApplicationServiceAdapter( DeploymentService::class ) )
}
