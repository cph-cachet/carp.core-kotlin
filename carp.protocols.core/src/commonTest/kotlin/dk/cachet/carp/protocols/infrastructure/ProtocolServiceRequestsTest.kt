package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.ServiceInvoker
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceMock
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s.
 */
class ProtocolServiceRequestsTest
{
    companion object
    {
        val requests: List<ProtocolServiceRequest> = listOf(
            ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" ),
            ProtocolServiceRequest.AddVersion( createComplexProtocol().getSnapshot(), "Updated" ),
            ProtocolServiceRequest.UpdateParticipantDataConfiguration( StudyProtocol.Id( UUID.randomUUID(), "Name" ), "Version", emptySet() ),
            ProtocolServiceRequest.GetBy( StudyProtocol.Id( UUID.randomUUID(), "Name" ), "Version" ),
            ProtocolServiceRequest.GetAllFor( UUID.randomUUID() ),
            ProtocolServiceRequest.GetVersionHistoryFor( StudyProtocol.Id( UUID.randomUUID(), "Name" ) )
        )
    }

    private val mock = ProtocolServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = ProtocolServiceRequest.serializer()
            val serialized = JSON.encodeToString( serializer, request )
            val parsed = JSON.decodeFromString( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runSuspendTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ProtocolService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }

    @Test
    fun invokeOn_deserialized_request_requires_copy() = runSuspendTest {
        val request = ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" )
        val serializer = ProtocolServiceRequest.serializer()
        val serialized = JSON.encodeToString( serializer, request )
        val parsed = JSON.decodeFromString( serializer, serialized ) as ProtocolServiceRequest.Add

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
