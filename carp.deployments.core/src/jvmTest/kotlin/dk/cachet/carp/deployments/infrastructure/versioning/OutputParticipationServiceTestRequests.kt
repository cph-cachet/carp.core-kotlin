package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.*
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator


class OutputParticipationServiceTestRequests :
    OutputTestRequests<ParticipationService>( ParticipationService::class ),
    ParticipationServiceTest
{
    override fun createSUT(): ParticipationServiceTest.SUT
    {
        val sut = ParticipationServiceHostTest.createSUT()

        val (loggedService, logger) = createLoggedApplicationService(
            sut.participationService,
            ::ParticipationServiceDecorator,
            EventBusLog(
                sut.eventBus,
                EventBusLog.Subscription( ParticipationService::class, ParticipationService.Event::class ),
                EventBusLog.Subscription( DeploymentService::class, DeploymentService.Event::class )
            )
        )

        serviceLogger = logger

        return ParticipationServiceTest.SUT( loggedService, sut.deploymentService, sut.accountService, sut.eventBus )
    }
}
