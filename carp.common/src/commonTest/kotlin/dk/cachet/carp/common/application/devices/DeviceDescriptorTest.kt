package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.BatteryAwareSamplingConfiguration
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
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
        val device = Smartphone( "Irrelevant", validConfigurations )

        device.validateDefaultSamplingConfiguration()
    }

    @Test
    fun validateModifiedDefaultSamplingConfigurations_with_invalid_configuration()
    {
        val invalidConfigurations = mapOf(
            Smartphone.Sensors.GEOLOCATION.dataType.type to NoOptionsSamplingConfiguration
        )
        val device = Smartphone( "Irrelevant", invalidConfigurations )

        assertFailsWith<IllegalStateException> { device.validateDefaultSamplingConfiguration() }
    }

    @Test
    fun getDefaultSamplingConfiguration_succeeds()
    {
        val typeMetaData = Smartphone.Sensors.GEOLOCATION
        val device = Smartphone( "Irrelevant" )

        val configuration = device.getDefaultSamplingConfiguration( typeMetaData.dataType.type )
        assertEquals( typeMetaData.default, configuration )
    }

    @Test
    fun getDefaultSamplingConfiguration_returns_overridden_defaultSamplingConfiguration()
    {
        val typeMetaData = Smartphone.Sensors.GEOLOCATION
        val dataType = typeMetaData.dataType.type
        val configurationOverride = BatteryAwareSamplingConfiguration(
            GranularitySamplingConfiguration( Granularity.Coarse ),
            GranularitySamplingConfiguration( Granularity.Coarse ),
            GranularitySamplingConfiguration( Granularity.Coarse )
        )
        val device = Smartphone( "Irrelevant", mapOf( dataType to configurationOverride ) )

        val configuration = device.getDefaultSamplingConfiguration( dataType )
        assertEquals( configurationOverride, configuration )
    }

    @Test
    fun getDefaultSamplingConfiguration_fails_for_unsupported_type()
    {
        val device = Smartphone( "Irrelevant" )

        val unsupportedType = DataType( "unsupported", "type" )
        assertFailsWith<IllegalArgumentException> { device.getDefaultSamplingConfiguration( unsupportedType ) }
    }
}
