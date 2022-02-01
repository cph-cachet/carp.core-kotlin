package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHostTest
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [ProtocolServiceRequest]'s.
 */
class ProtocolServiceRequestsTest : ApplicationServiceRequestsTest<ProtocolService, ProtocolServiceRequest>(
    ProtocolService::class,
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
            ProtocolServiceRequest.GetAllForOwner( UUID.randomUUID() ),
            ProtocolServiceRequest.GetVersionHistoryFor( UUID.randomUUID() )
        )
    }


    override fun createServiceLog(): ApplicationServiceLog<ProtocolService> =
        ProtocolServiceLog( ProtocolServiceHostTest.createService() )


    @Test
    fun invokeOn_deserialized_request_requires_copy() = runSuspendTest {
        val serviceLog = createServiceLog()

        val request = ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" )
        val serializer = ProtocolServiceRequest.serializer()
        val serialized = JSON.encodeToString( serializer, request )
        val parsed = JSON.decodeFromString( serializer, serialized ) as ProtocolServiceRequest.Add
        val service = serviceLog as ProtocolService

        // `ServiceInvoker` class delegation is not initialized as part of deserialization:
        // https://github.com/Kotlin/kotlinx.serialization/issues/241#issuecomment-555020729
        assertFails() // This throws a 'TypeError', which seems to be an inaccessible type.
        {
            parsed.invokeOn( service )
        }

        // But, it is initialized as part of copying the data object.
        // This is a suitable workaround for now for anyone that needs access to `ServiceInvoker`.
        val parsedCopy = parsed.copy()
        parsedCopy.invokeOn( service )
        assertTrue( serviceLog.wasCalled( request ) )
    }
}
