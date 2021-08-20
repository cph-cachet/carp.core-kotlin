package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.test.runSuspendTest
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
    fun openDataStreams_succeeds() = runSuspendTest {
        val service = createService()

        val dataStreamId = dataStreamId<StubData>( stubDeploymentId, "Some device" )
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId( dataStreamId )
        val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
        service.openDataStreams( configuration )

        val retrievedDataStream = service.getDataStream( dataStreamId, 0 )
        assertTrue( retrievedDataStream.isEmpty() )
    }

    @Test
    fun openDataStreams_fails_when_data_streams_already_opened() = runSuspendTest {
        val service = createServiceWithOpenStubDataStream()

        val expectedStream = DataStreamsConfiguration.ExpectedDataStream( "Some device", STUB_DATA_TYPE )
        val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
        assertFailsWith<IllegalStateException> { service.openDataStreams( configuration ) }
    }

    @Test
    fun appendToDataStreams_succeeds() = runSuspendTest {
        val service = createServiceWithOpenStubDataStream()

        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence( 0, StubData() )
        batch.appendSequence( sequence )
        service.appendToDataStreams( stubDeploymentId, batch )

        val retrievedSequence = service.getDataStream( sequence.dataStream, 0 )
        assertEquals( batch, retrievedSequence )
    }

    @Test
    fun appendToDataStreams_fails_for_preceding_sequence() = runSuspendTest {
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
    fun appendToDataStreams_fails_for_nonmatching_studyDeploymentId() = runSuspendTest {
        val service = createServiceWithOpenStubDataStream()

        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData(), StubData() ) )
        }
        val unknownDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( unknownDeploymentId, batch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_unexpected_data_stream() = runSuspendTest {
        val service = createServiceWithOpenStubDataStream()

        val unexpectedBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint() ) ) // `StubDataPoint` is unexpected.
        }
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( stubDeploymentId, unexpectedBatch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_closed_data_streams() = runSuspendTest {
        val service = createServiceWithOpenStubDataStream()
        service.closeDataStreams( setOf( stubDeploymentId ) )

        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData() ) )
        }
        assertFailsWith<IllegalStateException> { service.appendToDataStreams( stubDeploymentId, batch ) }
    }

    @Test
    fun getDataStream_fails_for_unopened_streams() = runSuspendTest {
        val service = createService()

        val unopenedStreamId = dataStreamId<StubData>( stubDeploymentId, stubSequenceDeviceRoleName )
        assertFailsWith<IllegalArgumentException> {
            service.getDataStream( unopenedStreamId, 0 )
        }
    }

    @Test
    fun closeDataStreams_succeeds() = runSuspendTest {
        val service = createServiceWithOpenStubDataStream()

        service.closeDataStreams( setOf( stubDeploymentId ) )
    }

    @Test
    fun closeDataStreams_fails_for_unopened_data_streams() = runSuspendTest {
        val service = createService()

        val unknownDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException>
        {
            service.closeDataStreams( setOf( unknownDeploymentId ) )
        }
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
