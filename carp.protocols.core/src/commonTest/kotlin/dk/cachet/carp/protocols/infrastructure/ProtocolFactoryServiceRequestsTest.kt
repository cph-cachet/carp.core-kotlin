package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
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


    override fun createServiceLoggingProxy(): ApplicationServiceLogger<ProtocolFactoryService, *>
    {
        val (loggedService, logger) = createLoggedApplicationService(
            ProtocolFactoryServiceHostTest.createService(),
            ::ProtocolFactoryServiceDecorator
        )

        // TODO: The base class relies on the proxied service also be a logger.
        return object :
            ApplicationServiceLogger<ProtocolFactoryService, ProtocolFactoryService.Event> by logger,
            ProtocolFactoryService by loggedService { }
    }
}
