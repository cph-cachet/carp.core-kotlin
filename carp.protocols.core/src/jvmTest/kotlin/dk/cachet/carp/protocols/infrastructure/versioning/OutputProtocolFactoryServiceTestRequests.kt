package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHostTest
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceTest
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceDecorator


class OutputProtocolFactoryServiceTestRequests :
    OutputTestRequests<ProtocolFactoryService>( ProtocolFactoryService::class ),
    ProtocolFactoryServiceTest
{
    override fun createService(): ProtocolFactoryService
    {
        val (loggedService, logger) = createLoggedApplicationService(
            ProtocolFactoryServiceHostTest.createService(),
            ::ProtocolFactoryServiceDecorator
        )

        serviceLogger = logger

        return loggedService
    }
}
