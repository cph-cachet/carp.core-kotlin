package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.BatteryAwareSamplingConfiguration
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
import kotlin.test.*


/**
 * Tests for [DeviceDescriptor].
 */
class DeviceDescriptorTest
{
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
