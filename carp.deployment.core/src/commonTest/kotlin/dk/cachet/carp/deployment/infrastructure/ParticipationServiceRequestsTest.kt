package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.UsernameAccountIdentity
import dk.cachet.carp.deployment.application.ParticipationService
import dk.cachet.carp.deployment.application.ParticipationServiceMock
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [ParticipationServiceRequest]'s.
 */
class ParticipationServiceRequestsTest
{
    companion object
    {
        val requests: List<ParticipationServiceRequest> = listOf(
            ParticipationServiceRequest.AddParticipation( UUID.randomUUID(), setOf( "Phone" ), UsernameAccountIdentity( "Test" ), StudyInvitation.empty() ),
            ParticipationServiceRequest.GetActiveParticipationInvitations( UUID.randomUUID() )
        )
    }

    private val mock = ParticipationServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = ParticipationServiceRequest.serializer()
            val serialized = JSON.encodeToString( serializer, request )
            val parsed = JSON.decodeFromString( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runSuspendTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ParticipationService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}
