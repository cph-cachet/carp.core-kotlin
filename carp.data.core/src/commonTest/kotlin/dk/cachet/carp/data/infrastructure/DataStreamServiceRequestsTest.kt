package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.MutableDataStreamBatch


class DataStreamServiceRequestsTest : ApplicationServiceRequestsTest<DataStreamService, DataStreamServiceRequest<*>>(
    DataStreamServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<DataStreamServiceRequest<*>> = listOf(
            DataStreamServiceRequest.OpenDataStreams( DataStreamsConfiguration( UUID.randomUUID(), emptySet() ) ),
            DataStreamServiceRequest.AppendToDataStreams( UUID.randomUUID(), MutableDataStreamBatch() ),
            DataStreamServiceRequest.GetDataStream(
                DataStreamId( UUID.randomUUID(), "Device", DataType( "some", "type" ) ),
                0
            ),
            DataStreamServiceRequest.CloseDataStreams( setOf( UUID.randomUUID() ) ),
            DataStreamServiceRequest.RemoveDataStreams( setOf( UUID.randomUUID() ) )
        )
    }


    override fun createServiceLoggingProxy(): ApplicationServiceLogger<DataStreamService, *>
    {
        val (loggedService, logger) = createLoggedApplicationService(
            InMemoryDataStreamService(),
            ::DataStreamServiceDecorator
        )

        // TODO: The base class relies on the proxied service also be a logger.
        return object :
            ApplicationServiceLogger<DataStreamService, DataStreamService.Event> by logger,
            DataStreamService by loggedService { }
    }
}
