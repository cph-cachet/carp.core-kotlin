package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHostTest


/**
 * Tests for [ProtocolFactoryServiceRequest]'s.
 */
class ProtocolFactoryServiceRequestsTest :
    ApplicationServiceRequestsTest<ProtocolFactoryService, ProtocolFactoryServiceRequest<*>>(
    ::ProtocolFactoryServiceDecorator,
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


    override fun createService() = ProtocolFactoryServiceHostTest.createService()
}
