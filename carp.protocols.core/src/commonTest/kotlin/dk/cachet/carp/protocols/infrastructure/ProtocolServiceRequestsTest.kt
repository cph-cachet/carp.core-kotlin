package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceMock
import dk.cachet.carp.protocols.domain.createComplexProtocol
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class ProtocolServiceRequestsTest
{
    companion object
    {
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
    fun invokeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ProtocolService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }

    @Test
    fun invokeOn_deserialized_request_requires_copy() = runBlockingTest {
        val request = ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" )
        val serializer = ProtocolServiceRequest.serializer()
        val serialized = JSON.stringify( serializer, request )
        val parsed = JSON.parse( serializer, serialized ) as ProtocolServiceRequest.Add

        // `ServiceInvoker` class delegation is not initialized as part of deserialization:
        // https://github.com/Kotlin/kotlinx.serialization/issues/241#issuecomment-555020729
        assertFails() // This throws a 'TypeError', which seems to be an inaccessible type.
        {
            parsed.invokeOn( mock )
        }

        // But, it is initialized as part of copying the data object.
        // This is a suitable workaround for now for anyone that needs access to `ServiceInvoker`.
        val parsedCopy = parsed.copy()
        parsedCopy.invokeOn( mock )
        assertTrue( mock.wasCalled( parsedCopy.function ) )
    }
}
