package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHostTest
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceTest
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceLoggingProxy


class OutputProtocolFactoryServiceTestRequests :
    OutputTestRequests<ProtocolFactoryService>(
        ProtocolFactoryService::class,
        ProtocolFactoryServiceLoggingProxy( ProtocolFactoryServiceHostTest.createService() )
    ),
    ProtocolFactoryServiceTest
{
    override fun createService(): ProtocolFactoryService = loggedService
}
