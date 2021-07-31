package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamServiceMock
import dk.cachet.carp.data.application.MutableDataStreamBatch


class DataStreamServiceRequestsTest : ApplicationServiceRequestsTest<DataStreamService, DataStreamServiceRequest>(
    DataStreamService::class,
    DataStreamServiceMock(),
    DataStreamServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        val REQUESTS: List<DataStreamServiceRequest> = listOf(
            DataStreamServiceRequest.AppendToDataStreams( UUID.randomUUID(), MutableDataStreamBatch() ),
            DataStreamServiceRequest.GetDataStream(
                DataStreamId( UUID.randomUUID(), "Device", DataType( "some", "type" ) ),
                0
            )
        )
    }
}
