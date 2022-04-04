package dk.cachet.carp.protocols.domain.configuration


/**
 * Test class for [EmptyProtocolTaskConfiguration].
 */
class EmptyProtocolTaskConfigurationTest : ProtocolTaskConfigurationTest
{
    override fun createTaskConfiguration(): ProtocolTaskConfiguration
    {
        return EmptyProtocolTaskConfiguration()
    }
}
