package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.infrastructure.InMemoryStudyProtocolRepository


/**
 * Tests for [ProtocolServiceHost].
 */
class ProtocolServiceHostTest : ProtocolServiceTest
{
    override fun createService(): ProtocolService = ProtocolServiceHost( InMemoryStudyProtocolRepository() )
}
