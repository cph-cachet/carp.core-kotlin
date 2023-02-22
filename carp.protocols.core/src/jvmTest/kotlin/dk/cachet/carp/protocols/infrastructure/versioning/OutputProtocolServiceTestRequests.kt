package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHostTest
import dk.cachet.carp.protocols.application.ProtocolServiceTest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceDecorator
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest


class OutputProtocolServiceTestRequests :
    OutputTestRequests<ProtocolService, ProtocolService.Event, ProtocolServiceRequest<*>>(
        ProtocolService::class,
        ::ProtocolServiceDecorator
    ),
    ProtocolServiceTest
{
    override fun createService(): ProtocolService =
        createLoggedApplicationService( ProtocolServiceHostTest.createService(), SingleThreadedEventBus() )
}
