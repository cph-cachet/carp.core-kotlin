package dk.cachet.carp.protocols.domain.configuration


/**
 * Test class for [EmptyProtocolDeviceConfiguration].
 */
class EmptyProtocolDeviceConfigurationTest : ProtocolDeviceConfigurationTest
{
    override fun createDeviceConfiguration(): ProtocolDeviceConfiguration
    {
        return EmptyProtocolDeviceConfiguration()
    }
}
