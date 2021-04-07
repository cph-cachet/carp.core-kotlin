package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployment.infrastructure.InMemoryParticipationRepository


/**
 * Tests for [ParticipationServiceHost].
 */
class ParticipationServiceHostTest : ParticipationServiceTest()
{
    override fun createService(
        participantDataInputTypes: InputDataTypeList
    ): Triple<ParticipationService, DeploymentService, AccountService>
    {
        val eventBus: EventBus = SingleThreadedEventBus()

        val deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        val accountService = InMemoryAccountService()

        val participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            accountService,
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )

        return Triple( participationService, deploymentService, accountService )
    }
}
