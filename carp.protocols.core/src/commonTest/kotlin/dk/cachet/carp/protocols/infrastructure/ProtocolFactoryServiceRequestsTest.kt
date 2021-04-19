package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceMock


/**
 * Tests for [ProtocolFactoryServiceRequest]'s.
 */
class ProtocolFactoryServiceRequestsTest : ApplicationServiceRequestsTest<ProtocolFactoryService, ProtocolFactoryServiceRequest>(
    ProtocolFactoryService::class,
    ProtocolFactoryServiceMock(),
    ProtocolFactoryServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ProtocolFactoryServiceRequest> = listOf(
            ProtocolFactoryServiceRequest.CreateCustomProtocol( UUID.randomUUID(), "Name", "...", "Description" )
        )
    }
}
