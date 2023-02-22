package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHostTest
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol


/**
 * Tests for [ProtocolServiceRequest]'s.
 */
class ProtocolServiceRequestsTest : ApplicationServiceRequestsTest<ProtocolService, ProtocolServiceRequest<*>>(
    ::ProtocolServiceDecorator,
    ProtocolServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ProtocolServiceRequest<*>> = listOf(
            ProtocolServiceRequest.Add( createComplexProtocol().getSnapshot(), "Initial" ),
            ProtocolServiceRequest.AddVersion( createComplexProtocol().getSnapshot(), "Updated" ),
            ProtocolServiceRequest.UpdateParticipantDataConfiguration( UUID.randomUUID(), "Version", emptySet() ),
            ProtocolServiceRequest.GetBy( UUID.randomUUID(), "Version" ),
            ProtocolServiceRequest.GetAllForOwner( UUID.randomUUID() ),
            ProtocolServiceRequest.GetVersionHistoryFor( UUID.randomUUID() )
        )
    }


    override fun createService() = ProtocolServiceHostTest.createService()
}
