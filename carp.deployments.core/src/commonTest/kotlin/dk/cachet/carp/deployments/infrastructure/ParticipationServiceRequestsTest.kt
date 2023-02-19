package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHostTest


/**
 * Tests for [ParticipationServiceRequest]'s.
 */
class ParticipationServiceRequestsTest : ApplicationServiceRequestsTest<ParticipationService, ParticipationServiceRequest<*>>(
    ParticipationServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ParticipationServiceRequest<*>> = listOf(
            ParticipationServiceRequest.GetActiveParticipationInvitations( UUID.randomUUID() ),
            ParticipationServiceRequest.GetParticipantData( UUID.randomUUID() ),
            ParticipationServiceRequest.GetParticipantDataList( emptySet() ),
            ParticipationServiceRequest.SetParticipantData(
                UUID.randomUUID(),
                mapOf( CarpInputDataTypes.SEX to Sex.Male )
            )
        )
    }


    override fun createServiceLoggingProxy(): ApplicationServiceLogger<ParticipationService, *>
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

        // TODO: The base class relies on the proxied service also be a logger.
        return object :
            ApplicationServiceLogger<ParticipationService, ParticipationService.Event> by logger,
            ParticipationService by loggedService { }
    }
}
