package dk.cachet.carp.protocols.application


/**
 * Tests for [ProtocolFactoryServiceHost].
 */
class ProtocolFactoryServiceHostTest : ProtocolFactoryServiceTest
{
    override fun createService(): ProtocolFactoryService = ProtocolFactoryServiceHost()
}
