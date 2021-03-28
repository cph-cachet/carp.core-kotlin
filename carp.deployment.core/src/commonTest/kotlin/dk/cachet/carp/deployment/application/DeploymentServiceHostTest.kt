package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.application.EventBus
import dk.cachet.carp.common.infrastructure.SingleThreadedEventBus
import dk.cachet.carp.common.application.createApplicationServiceAdapter
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository


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
