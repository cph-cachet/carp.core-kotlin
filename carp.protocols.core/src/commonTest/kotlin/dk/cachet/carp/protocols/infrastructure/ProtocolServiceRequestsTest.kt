package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHostTest
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol


/**
 * Tests for [ProtocolServiceRequest]'s.
 */
class ProtocolServiceRequestsTest : ApplicationServiceRequestsTest<ProtocolService, ProtocolServiceRequest<*>>(
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


    override fun createServiceLoggingProxy(): ApplicationServiceLogger<ProtocolService, *>
    {
        val (loggedService, logger) = createLoggedApplicationService(
            ProtocolServiceHostTest.createService(),
            ::ProtocolServiceDecorator
        )

        // TODO: The base class relies on the proxied service also be a logger.
        return object :
            ApplicationServiceLogger<ProtocolService, ProtocolService.Event> by logger,
            ProtocolService by loggedService { }
    }
}
