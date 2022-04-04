package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.TestUUIDFactory
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.test.TestClock


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest
{
    companion object
    {
        fun createService(): ParticipationServiceTest.DependentServices
        {
            val eventBus: EventBus = SingleThreadedEventBus()

            val deploymentService = DeploymentServiceHost(
                InMemoryDeploymentRepository(),
                InMemoryDataStreamService(),
                eventBus.createApplicationServiceAdapter( DeploymentService::class ),
                TestClock
            )

            val accountService = InMemoryAccountService( TestUUIDFactory() )

            val participationService = ParticipationServiceHost(
                InMemoryParticipationRepository(),
                ParticipantGroupService( accountService ),
                eventBus.createApplicationServiceAdapter( ParticipationService::class )
            )

            return ParticipationServiceTest.DependentServices(
                participationService,
                deploymentService,
                accountService,
                eventBus
            )
        }
    }

    override fun createService(): ParticipationServiceTest.DependentServices =
        ParticipationServiceHostTest.createService()
}
