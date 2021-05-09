package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceMock
import dk.cachet.carp.deployments.application.users.StudyInvitation


/**
 * Tests for [ParticipationServiceRequest]'s.
 */
class ParticipationServiceRequestsTest : ApplicationServiceRequestsTest<ParticipationService, ParticipationServiceRequest>(
    ParticipationService::class,
    ParticipationServiceMock(),
    ParticipationServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ParticipationServiceRequest> = listOf(
            ParticipationServiceRequest.AddParticipation( UUID.randomUUID(), UUID.randomUUID(), setOf( "Phone" ), UsernameAccountIdentity( "Test" ), StudyInvitation.empty() ),
            ParticipationServiceRequest.GetActiveParticipationInvitations( UUID.randomUUID() ),
            ParticipationServiceRequest.GetParticipantData( UUID.randomUUID() ),
            ParticipationServiceRequest.GetParticipantDataList( emptySet() ),
            ParticipationServiceRequest.SetParticipantData( UUID.randomUUID(), CarpInputDataTypes.SEX, Sex.Male )
        )
    }
}
