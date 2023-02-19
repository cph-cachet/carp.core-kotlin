package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamServiceTest
import dk.cachet.carp.data.infrastructure.DataStreamServiceLoggingProxy
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService


class OutputDataStreamServiceTestRequests :
    OutputTestRequests<DataStreamService>( DataStreamService::class ),
    DataStreamServiceTest
{
    override fun createService(): DataStreamService =
        DataStreamServiceLoggingProxy( InMemoryDataStreamService(), SingleThreadedEventBus() )
            .also { serviceLogger = it }
}
