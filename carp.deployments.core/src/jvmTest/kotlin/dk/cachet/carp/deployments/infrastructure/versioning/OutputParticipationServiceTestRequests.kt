package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHostTest
import dk.cachet.carp.deployments.application.ParticipationServiceTest
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceLoggingProxy


private val services = ParticipationServiceHostTest.createService()

class OutputParticipationServiceTestRequests :
    OutputTestRequests<ParticipationService>(
        ParticipationService::class,
        ParticipationServiceLoggingProxy( services.participationService, services.eventBus )
    ),
    ParticipationServiceTest
{
    override fun createService() =
        ParticipationServiceTest.DependentServices(
            loggedService,
            services.deploymentService,
            services.accountService,
            services.eventBus
        )
}
