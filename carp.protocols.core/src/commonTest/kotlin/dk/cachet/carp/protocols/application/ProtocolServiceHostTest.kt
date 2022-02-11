package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.infrastructure.InMemoryStudyProtocolRepository
import dk.cachet.carp.test.TestClock


/**
 * Tests for [ProtocolServiceHost].
 */
class ProtocolServiceHostTest : ProtocolServiceTest
{
    companion object
    {
        fun createService(): ProtocolService = ProtocolServiceHost( InMemoryStudyProtocolRepository(), TestClock )
    }

    override fun createService(): ProtocolService = ProtocolServiceHostTest.createService()
}
