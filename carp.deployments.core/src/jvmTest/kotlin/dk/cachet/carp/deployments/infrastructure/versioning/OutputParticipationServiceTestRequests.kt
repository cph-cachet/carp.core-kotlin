package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.*
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest


class OutputParticipationServiceTestRequests :
    OutputTestRequests<ParticipationService, ParticipationService.Event, ParticipationServiceRequest<*>>(
        ParticipationService::class,
        ::ParticipationServiceDecorator
    ),
    ParticipationServiceTest
{
    override fun createSUT(): ParticipationServiceTest.SUT
    {
        val sut = ParticipationServiceHostTest.createSUT()
        val loggedService = createLoggedApplicationService( sut.participationService, sut.eventBus )

        return ParticipationServiceTest.SUT( loggedService, sut.deploymentService, sut.accountService, sut.eventBus )
    }
}
