package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.UsernameAccountIdentity
import dk.cachet.carp.deployment.application.ParticipationService
import dk.cachet.carp.deployment.application.ParticipationServiceMock
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [ParticipationServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class ParticipationServiceRequestsTest
{
    companion object
    {
        val requests: List<ParticipationServiceRequest> = listOf(
            ParticipationServiceRequest.AddParticipation( UUID.randomUUID(), UsernameAccountIdentity( "Test" ), StudyInvitation.empty() ),
            ParticipationServiceRequest.GetParticipationsForStudyDeployment( UUID.randomUUID() )
        )
    }

    private val mock = ParticipationServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = ParticipationServiceRequest.serializer()
            val serialized = JSON.stringify( serializer, request )
            val parsed = JSON.parse( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ParticipationService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}
