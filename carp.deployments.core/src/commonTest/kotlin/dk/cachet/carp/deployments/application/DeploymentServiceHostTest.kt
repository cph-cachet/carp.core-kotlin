package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository


/**
 * Tests for [DeploymentServiceHost].
 */
class DeploymentServiceHostTest : DeploymentServiceTest
{
    companion object
    {
        fun createService(): DeploymentService
        {
            val eventBus: EventBus = SingleThreadedEventBus()

            return DeploymentServiceHost(
                InMemoryDeploymentRepository(),
                InMemoryDataStreamService(),
                eventBus.createApplicationServiceAdapter( DeploymentService::class )
            )
        }
    }

    override fun createService(): DeploymentService = DeploymentServiceHostTest.createService()
}
