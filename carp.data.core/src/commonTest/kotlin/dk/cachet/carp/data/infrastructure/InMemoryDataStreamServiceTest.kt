package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamServiceTest


/**
 * Tests for [InMemoryDataStreamService].
 */
class InMemoryDataStreamServiceTest : DataStreamServiceTest
{
    override fun createService(): DataStreamService = InMemoryDataStreamService()
}
