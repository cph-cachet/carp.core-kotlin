package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamServiceTest
import dk.cachet.carp.data.infrastructure.DataStreamServiceLoggingProxy
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService


class OutputDataServiceTestRequests :
    OutputTestRequests<DataStreamService>(
        DataStreamService::class,
        DataStreamServiceLoggingProxy( InMemoryDataStreamService() )
    ),
    DataStreamServiceTest
{
    override fun createService(): DataStreamService = loggedService
}
