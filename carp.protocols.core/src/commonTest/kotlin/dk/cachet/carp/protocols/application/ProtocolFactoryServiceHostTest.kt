package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.test.infrastructure.TestUUIDFactory
import dk.cachet.carp.test.TestClock


/**
 * Tests for [ProtocolFactoryServiceHost].
 */
class ProtocolFactoryServiceHostTest : ProtocolFactoryServiceTest
{
    companion object
    {
        fun createService(): ProtocolFactoryService = ProtocolFactoryServiceHost( TestUUIDFactory(), TestClock )
    }

    override fun createService(): ProtocolFactoryService = ProtocolFactoryServiceHostTest.createService()
}
