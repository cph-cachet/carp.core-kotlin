package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHostTest
import dk.cachet.carp.deployments.application.ParticipationServiceTest
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceLoggingProxy


class OutputParticipationServiceTestRequests :
    OutputTestRequests<ParticipationService>( ParticipationService::class ),
    ParticipationServiceTest
{
    override fun createSUT(): ParticipationServiceTest.SUT
    {
        val services = ParticipationServiceHostTest.createSUT()
        val service = ParticipationServiceLoggingProxy( services.participationService, services.eventBus )
        serviceLogger = service

        return ParticipationServiceTest.SUT(
            service,
            services.deploymentService,
            services.accountService,
            services.eventBus
        )
    }
}
