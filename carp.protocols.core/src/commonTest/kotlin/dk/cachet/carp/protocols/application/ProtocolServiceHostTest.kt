package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.infrastructure.InMemoryStudyProtocolRepository


/**
 * Tests for [ProtocolServiceHost].
 */
class ProtocolServiceHostTest : ProtocolServiceTest
{
    companion object
    {
        fun createService(): ProtocolService = ProtocolServiceHost( InMemoryStudyProtocolRepository() )
    }

    override fun createService(): ProtocolService = ProtocolServiceHostTest.createService()
}
