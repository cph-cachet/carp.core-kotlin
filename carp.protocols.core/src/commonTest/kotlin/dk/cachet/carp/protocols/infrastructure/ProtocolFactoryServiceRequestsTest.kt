package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHostTest


/**
 * Tests for [ProtocolFactoryServiceRequest]'s.
 */
class ProtocolFactoryServiceRequestsTest : ApplicationServiceRequestsTest<ProtocolFactoryService, ProtocolFactoryServiceRequest<*>>(
    ProtocolFactoryServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<ProtocolFactoryServiceRequest<*>> = listOf(
            ProtocolFactoryServiceRequest.CreateCustomProtocol( UUID.randomUUID(), "Name", "...", "Description" )
        )
    }


    override fun createServiceLog(): ApplicationServiceLog<ProtocolFactoryService, ProtocolFactoryService.Event> =
        ProtocolFactoryServiceLog( ProtocolFactoryServiceHostTest.createService() )
}
