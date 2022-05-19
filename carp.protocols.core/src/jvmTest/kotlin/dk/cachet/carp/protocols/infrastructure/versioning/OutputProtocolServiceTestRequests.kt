package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHostTest
import dk.cachet.carp.protocols.application.ProtocolServiceTest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceLoggingProxy


class OutputProtocolServiceTestRequests :
    OutputTestRequests<ProtocolService>( ProtocolService::class ),
    ProtocolServiceTest
{
    override fun createService(): ProtocolService =
        ProtocolServiceLoggingProxy( ProtocolServiceHostTest.createService(), SingleThreadedEventBus() )
            .also { loggedService = it }
}
