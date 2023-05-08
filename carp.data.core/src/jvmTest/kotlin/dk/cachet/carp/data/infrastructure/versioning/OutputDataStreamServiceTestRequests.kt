package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamServiceTest
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService


class OutputDataStreamServiceTestRequests :
    OutputTestRequests<DataStreamService, DataStreamService.Event, DataStreamServiceRequest<*>>(
        DataStreamService::class,
        ::DataStreamServiceDecorator
    ),
    DataStreamServiceTest
{
    override fun createService(): DataStreamService =
        createLoggedApplicationService( InMemoryDataStreamService(), SingleThreadedEventBus() )
}
