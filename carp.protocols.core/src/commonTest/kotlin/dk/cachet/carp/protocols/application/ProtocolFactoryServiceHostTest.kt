package dk.cachet.carp.protocols.application


/**
 * Tests for [ProtocolFactoryServiceHost].
 */
class ProtocolFactoryServiceHostTest : ProtocolFactoryServiceTest
{
    companion object
    {
        fun createService(): ProtocolFactoryService = ProtocolFactoryServiceHost()
    }

    override fun createService(): ProtocolFactoryService = ProtocolFactoryServiceHostTest.createService()
}
