package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for implementations of [DataStreamService].
 */
interface DataStreamServiceTest
{
    /**
     * Create a [DataStreamService] to be used in the tests.
     */
    fun createService(): DataStreamService


    @Test
    fun openDataStreams_succeeds() = runTest {
        val service = createService()

        val dataStreamId = dataStreamId<StubData>( stubDeploymentId, "Some device" )
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId( dataStreamId )
        val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
        service.openDataStreams( configuration )

        val retrievedDataStream = service.getDataStream( dataStreamId, 0 )
        assertTrue( retrievedDataStream.isEmpty() )
    }

    @Test
    fun openDataStreams_fails_when_data_streams_already_opened() = runTest {
        val service = createServiceWithOpenStubDataStream()

        val expectedStream = DataStreamsConfiguration.ExpectedDataStream( "Some device", STUB_DATA_TYPE )
        val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
        assertFailsWith<IllegalStateException> { service.openDataStreams( configuration ) }
    }

    @Test
    fun appendToDataStreams_succeeds() = runTest {
        val service = createServiceWithOpenStubDataStream()

        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence( 0, StubData() )
        batch.appendSequence( sequence )
        service.appendToDataStreams( stubDeploymentId, batch )

        val retrievedSequence = service.getDataStream( sequence.dataStream, 0 )
        assertEquals( batch, retrievedSequence )
    }

    @Test
    fun appendToDataStreams_fails_for_preceding_sequence() = runTest {
        val service = createServiceWithOpenStubDataStream()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData(), StubData() ) )
        }
        service.appendToDataStreams( stubDeploymentId, batch )

        val appendBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 1, StubData() ) )
        }
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( stubDeploymentId, appendBatch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_nonmatching_studyDeploymentId() = runTest {
        val service = createServiceWithOpenStubDataStream()

        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData(), StubData() ) )
        }
        val unknownDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( unknownDeploymentId, batch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_unexpected_data_stream() = runTest {
        val service = createServiceWithOpenStubDataStream()

        val unexpectedBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint() ) ) // `StubDataPoint` is unexpected.
        }
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( stubDeploymentId, unexpectedBatch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_closed_data_streams() = runTest {
        val service = createServiceWithOpenStubDataStream()
        service.closeDataStreams( setOf( stubDeploymentId ) )

        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData() ) )
        }
        assertFailsWith<IllegalStateException> { service.appendToDataStreams( stubDeploymentId, batch ) }
    }

    @Test
    fun getDataStream_fails_for_unopened_streams() = runTest {
        val service = createService()

        val unopenedStreamId = dataStreamId<StubData>( stubDeploymentId, stubSequenceDeviceRoleName )
        assertFailsWith<IllegalArgumentException> {
            service.getDataStream( unopenedStreamId, 0 )
        }
    }

    @Test
    fun closeDataStreams_succeeds() = runTest {
        val service = createServiceWithOpenStubDataStream()

        service.closeDataStreams( setOf( stubDeploymentId ) )
    }

    @Test
    fun closeDataStreams_fails_for_unopened_data_streams() = runTest {
        val service = createService()

        val unknownDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException>
        {
            service.closeDataStreams( setOf( unknownDeploymentId ) )
        }
    }

    @Test
    fun removeDataStreams_succeeds() = runTest {
        val service = createServiceWithOpenStubDataStream()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData(), StubData() ) )
        }
        service.appendToDataStreams( stubDeploymentId, batch )

        val removed = service.removeDataStreams( setOf( stubDeploymentId ) )
        assertEquals( setOf( stubDeploymentId ), removed )

        val dataStreamId = dataStreamId<StubData>( stubDeploymentId, stubSequenceDeviceRoleName )
        assertFailsWith<IllegalArgumentException> { service.getDataStream( dataStreamId, 0 ) }
    }

    @Test
    fun removeDataStream_ignores_unknown_ids() = runTest {
        val service = createServiceWithOpenStubDataStream()

        val unknownDeploymentid = UUID.randomUUID()
        val removed = service.removeDataStreams( setOf( stubDeploymentId, unknownDeploymentid ) )

        assertEquals( setOf( stubDeploymentId ), removed )
    }


    /**
     * Create a data stream service and open [stubDataStream] for [stubDeploymentId].
     */
    private suspend fun createServiceWithOpenStubDataStream(): DataStreamService =
        createService().apply {
            // Device name corresponds to the one created by `createStubSequence`.
            val expectedStream = DataStreamsConfiguration.ExpectedDataStream( stubSequenceDeviceRoleName, STUB_DATA_TYPE )

            val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
            openDataStreams( configuration )
        }
}
