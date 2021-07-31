package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubData
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
    fun appendToDataStreams_succeeds() = runSuspendTest {
        val service = createService()

        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence( 0, StubData() )
        batch.appendSequence( sequence )
        service.appendToDataStreams( stubDeploymentId, batch )

        val retrievedSequence = service.getDataStream( sequence.dataStream, 0 )
        assertEquals( batch, retrievedSequence )
    }

    @Test
    fun appendToDataStreams_fails_for_preceding_sequence() = runSuspendTest {
        val service = createService()
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
        val service = createService()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubData(), StubData() ) )
        }

        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( UUID.randomUUID(), batch ) }
    }

    @Test
    fun getDataStream_is_empty_for_nonexisting_stream() = runSuspendTest {
        val service = createService()

        val streamId = dataStreamId<StubData>( UUID.randomUUID(), "Device" )
        val stream = service.getDataStream( streamId, 0 )

        assertTrue( stream.isEmpty() )
    }
}
