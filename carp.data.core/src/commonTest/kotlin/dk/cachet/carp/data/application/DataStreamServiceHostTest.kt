package dk.cachet.carp.data.application

import dk.cachet.carp.data.infrastructure.InMemoryDataStreamRepository


/**
 * Tests for [DataStreamServiceHost].
 */
class DataStreamServiceHostTest : DataStreamServiceTest
{
    override fun createService(): DataStreamService = DataStreamServiceHost( InMemoryDataStreamRepository() )
}
