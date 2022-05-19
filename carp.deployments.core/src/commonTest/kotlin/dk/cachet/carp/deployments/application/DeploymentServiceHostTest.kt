package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.test.TestClock


/**
 * Tests for [DeploymentServiceHost].
 */
class DeploymentServiceHostTest : DeploymentServiceTest
{
    companion object
    {
        fun createService(): Pair<DeploymentService, EventBus>
        {
            val eventBus: EventBus = SingleThreadedEventBus()

            val deploymentService = DeploymentServiceHost(
                InMemoryDeploymentRepository(),
                InMemoryDataStreamService(),
                eventBus.createApplicationServiceAdapter( DeploymentService::class ),
                TestClock
            )

            return Pair( deploymentService, eventBus )
        }
    }

    override fun createService(): DeploymentService = DeploymentServiceHostTest.createService().first
}
