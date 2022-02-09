package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest
{
    data class DependentServices(
        val participationService: ParticipationService,
        val deploymentService: DeploymentService,
        val accountService: AccountService,
        val eventBus: EventBus
    )

    companion object
    {
        fun createService(): DependentServices
        {
            val eventBus: EventBus = SingleThreadedEventBus()

            val deploymentService = DeploymentServiceHost(
                InMemoryDeploymentRepository(),
                InMemoryDataStreamService(),
                eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

            val accountService = InMemoryAccountService()

            val participationService = ParticipationServiceHost(
                InMemoryParticipationRepository(),
                ParticipantGroupService( accountService ),
                eventBus.createApplicationServiceAdapter( ParticipationService::class ) )

            return DependentServices( participationService, deploymentService, accountService, eventBus )
        }
    }

    override fun createService(): Triple<ParticipationService, DeploymentService, AccountService> =
        ParticipationServiceHostTest
            .createService()
            .let { Triple( it.participationService, it.deploymentService, it.accountService ) }
}
