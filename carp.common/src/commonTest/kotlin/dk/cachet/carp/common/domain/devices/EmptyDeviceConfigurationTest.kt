package dk.cachet.carp.common.domain.devices


/**
 * Test class for [EmptyDeviceConfiguration].
 */
class EmptyDeviceConfigurationTest : DeviceConfigurationTest
{
    override fun createDeviceConfiguration(): DeviceConfiguration
    {
        return EmptyDeviceConfiguration()
    }
}
