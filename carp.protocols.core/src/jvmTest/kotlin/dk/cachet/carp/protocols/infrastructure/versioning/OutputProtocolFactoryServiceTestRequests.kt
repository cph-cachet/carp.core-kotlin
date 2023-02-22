package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHostTest
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceTest
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceDecorator
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest


class OutputProtocolFactoryServiceTestRequests :
    OutputTestRequests<ProtocolFactoryService, ProtocolFactoryService.Event, ProtocolFactoryServiceRequest<*>>(
        ProtocolFactoryService::class,
        ::ProtocolFactoryServiceDecorator
    ),
    ProtocolFactoryServiceTest
{
    override fun createService(): ProtocolFactoryService =
        createLoggedApplicationService( ProtocolFactoryServiceHostTest.createService(), SingleThreadedEventBus() )
}
