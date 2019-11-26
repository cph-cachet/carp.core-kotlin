package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.*
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.studies.application.*
import dk.cachet.carp.studies.domain.users.Username
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [UserServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class UserServiceRequestsTest
{
    companion object {
        val requests: List<UserServiceRequest> = listOf(
            UserServiceRequest.CreateAccountWithUsername( Username( "Test" ) ),
            UserServiceRequest.CreateAccountWithEmailAddress( EmailAddress( "test@test.com" ) ),
            UserServiceRequest.CreateParticipant( UUID.randomUUID(), UUID.randomUUID() ),
            UserServiceRequest.InviteParticipant( UUID.randomUUID(), EmailAddress( "test@test.com" ) ),
            UserServiceRequest.GetParticipantsForStudy( UUID.randomUUID() )
        )
    }

    private val mock = UserServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = UserServiceRequest.serializer()
            val serialized = JSON.stringify( serializer, request )
            val parsed = JSON.parse( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<UserService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}