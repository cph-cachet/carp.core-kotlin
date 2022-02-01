package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService


/**
 * Tests for [ParticipationServiceRequest]'s.
 */
class ParticipationServiceRequestsTest : ApplicationServiceRequestsTest<ParticipationService, ParticipationServiceRequest>(
    ParticipationService::class,
    ParticipationServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ParticipationServiceRequest> = listOf(
            ParticipationServiceRequest.GetActiveParticipationInvitations( UUID.randomUUID() ),
            ParticipationServiceRequest.GetParticipantData( UUID.randomUUID() ),
            ParticipationServiceRequest.GetParticipantDataList( emptySet() ),
            ParticipationServiceRequest.SetParticipantData(
                UUID.randomUUID(),
                mapOf( CarpInputDataTypes.SEX to Sex.Male )
            )
        )
    }


    override fun createServiceLog(): ApplicationServiceLog<ParticipationService> = ParticipationServiceLog(
        ParticipationServiceHost(
            InMemoryParticipationRepository(),
            ParticipantGroupService( InMemoryAccountService() ),
            SingleThreadedEventBus().createApplicationServiceAdapter( ParticipationService::class )
        )
    )
}
