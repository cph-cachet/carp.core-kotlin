package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
import kotlin.test.*


/**
 * Tests for [Smartphone] and [SmartphoneBuilder].
 */
class SmartphoneTest
{
    @Test
    fun builder_sets_sampling_configuration()
    {
        val configuredGranularity = Granularity.Balanced

        val phone = Smartphone( "Test" )
        {
            defaultSamplingConfiguration {
                geolocation { granularity = configuredGranularity }
            }
        }

        val type = Smartphone.Sensors.GEOLOCATION.type
        val configuration = phone.defaultSamplingConfiguration[ type ] as GranularitySamplingConfiguration
        assertEquals( configuredGranularity, configuration.granularity )
    }
}
