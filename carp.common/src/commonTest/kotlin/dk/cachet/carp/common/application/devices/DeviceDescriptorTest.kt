package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfiguration
import kotlin.test.*


/**
 * Tests for [DeviceDescriptor].
 */
class DeviceDescriptorTest
{
    @Test
    fun validateModifiedDefaultSamplingConfigurations_with_correct_configuration()
    {
        val validConfigurations = mapOf(
            Smartphone.Sensors.GEOLOCATION.dataType.type to Smartphone.Sensors.GEOLOCATION.default
        )
        val device = Smartphone( "Irrelevant", false, validConfigurations )

        device.validateDefaultSamplingConfiguration()
    }

    @Test
    fun validateModifiedDefaultSamplingConfigurations_with_invalid_configuration()
    {
        val invalidConfigurations = mapOf(
            Smartphone.Sensors.GEOLOCATION.dataType.type to NoOptionsSamplingConfiguration
        )
        val device = Smartphone( "Irrelevant", false, invalidConfigurations )

        assertFailsWith<IllegalStateException> { device.validateDefaultSamplingConfiguration() }
    }
}
