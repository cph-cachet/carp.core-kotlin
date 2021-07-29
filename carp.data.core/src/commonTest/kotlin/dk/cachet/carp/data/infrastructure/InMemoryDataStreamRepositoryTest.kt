package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.data.domain.DataStreamRepository
import dk.cachet.carp.data.domain.DataStreamRepositoryTest


/**
 * Tests for [InMemoryDataStreamRepository].
 */
class InMemoryDataStreamRepositoryTest : DataStreamRepositoryTest
{
    override fun createRepository(): DataStreamRepository = InMemoryDataStreamRepository()
}
