package dk.cachet.carp.data.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.application.createStubSequence
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for implementations of [DataStreamRepository].
 */
interface DataStreamRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createRepository(): DataStreamRepository


    @Test
    fun appendToDataStream_succeeds() = runSuspendTest {
        val repo = createRepository()

        val sequence = createStubSequence( 0, StubData() )
        repo.appendToDataStream( sequence )

        val retrieved = repo.getDataStream( sequence.dataStream, 0 )
        assertEquals( sequence.toList(), retrieved.toList() )
    }

    @Test
    fun appendToDataStream_fails_for_preceding_sequence() = runSuspendTest {
        val repo = createRepository()
        val sequence = createStubSequence( 5, StubData(), StubData() )
        repo.appendToDataStream( sequence )

        val precedingSequence = createStubSequence( 0, StubData() )
        assertFailsWith<IllegalArgumentException> { repo.appendToDataStream( precedingSequence ) }

        val startOverlapsSequence = createStubSequence( 6, StubData() )
        assertFailsWith<IllegalArgumentException> { repo.appendToDataStream( startOverlapsSequence ) }
    }

    @Test
    fun getDataStream_returns_correct_stream() = runSuspendTest {
        val repo = createRepository()
        val stubSequence = createStubSequence( 0, StubData() )
        repo.appendToDataStream( stubSequence )
        val stubPointSequence = createStubSequence( 0, StubDataPoint() )
        repo.appendToDataStream( stubPointSequence )

        val retrieved = repo.getDataStream( stubSequence.dataStream, 0 )

        assertEquals( stubSequence.toList(), retrieved.toList() )
    }

    @Test
    fun getDataStream_returns_sub_sequence() = runSuspendTest {
        val repo = createRepository()
        val allMeasures = arrayOf(
            measurement( StubData(), 0 ),
            measurement( StubData(), 1 ),
            measurement( StubData(), 5 ),
            measurement( StubData(), 6 )
        )
        val firstSequence = createStubSequence( 0, *allMeasures.copyOfRange( 0, 2 ) )
        val secondSequence = createStubSequence( 5, *allMeasures.copyOfRange( 2, 4 ) )
        repo.appendToDataStream( firstSequence )
        repo.appendToDataStream( secondSequence )

        val retrieved = repo
            .getDataStream( firstSequence.dataStream, 1L..5 )
            .map { it.measurement }
            .toList()

        assertEquals( allMeasures.slice( 1..2 ), retrieved )
    }

    @Test
    fun getDataStream_returns_empty_for_unknown_stream() = runSuspendTest {
        val repo = createRepository()

        val unknownStream = dataStreamId<StubData>( UUID.randomUUID(), "Device" )
        val retrieved = repo.getDataStream( unknownStream, 0 )

        assertTrue( retrieved.isEmpty() )
    }

    @Test
    fun getDataStream_returns_empty_for_out_of_range_sequence() = runSuspendTest {
        val repo = createRepository()
        val sequence = createStubSequence( 0, StubData() )
        repo.appendToDataStream( sequence )

        val retrieved = repo.getDataStream( sequence.dataStream, 1 )

        assertTrue( retrieved.isEmpty() )
    }
}
