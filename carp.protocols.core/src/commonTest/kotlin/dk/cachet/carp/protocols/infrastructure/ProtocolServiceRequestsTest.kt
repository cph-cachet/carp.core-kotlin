package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceMock
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s.
 */
class ProtocolServiceRequestsTest : ApplicationServiceRequestsTest<ProtocolService, ProtocolServiceRequest>(
    ProtocolService::class,
    ProtocolServiceMock(),
    ProtocolServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ProtocolServiceRequest> = listOf(
            ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" ),
            ProtocolServiceRequest.AddVersion( createComplexProtocol().getSnapshot(), "Updated" ),
            ProtocolServiceRequest.UpdateParticipantDataConfiguration( UUID.randomUUID(), "Version", emptySet() ),
            ProtocolServiceRequest.GetBy( UUID.randomUUID(), "Version" ),
            ProtocolServiceRequest.GetAllFor( UUID.randomUUID() ),
            ProtocolServiceRequest.GetVersionHistoryFor( UUID.randomUUID() )
        )
    }


    @Test
    fun invokeOn_deserialized_request_requires_copy() = runSuspendTest {
        val request = ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" )
        val serializer = ProtocolServiceRequest.serializer()
        val serialized = JSON.encodeToString( serializer, request )
        val parsed = JSON.decodeFromString( serializer, serialized ) as ProtocolServiceRequest.Add
        val mock = serviceMock as ProtocolService

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
        assertTrue( serviceMock.wasCalled( parsedCopy.function ) )
    }
}
