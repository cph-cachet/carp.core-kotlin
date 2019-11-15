package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.protocols.application.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class ProtocolServiceRequestsTest
{
    companion object {
        val requests: List<ProtocolServiceRequest> = listOf(
            ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" ),
            ProtocolServiceRequest.Update( createComplexProtocol().getSnapshot(), "Updated" ),
            ProtocolServiceRequest.GetBy( ProtocolOwner(), "Name", "Version" ),
            ProtocolServiceRequest.GetAllFor( ProtocolOwner() ),
            ProtocolServiceRequest.GetVersionHistoryFor( ProtocolOwner(), "Name" )
        )
    }

    private val mock = ProtocolServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = ProtocolServiceRequest.serializer()
            val serialized = JSON.stringify( serializer, request )
            val parsed = JSON.parse( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun executeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ProtocolService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = ProtocolService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = requests.map {
            val serviceInvoker = it as ServiceInvoker<ProtocolService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}