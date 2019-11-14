package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.protocols.application.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s.
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

    @Test
    fun executeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ProtocolService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function ) )
            mock.reset()
        }
    }
}