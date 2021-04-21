package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.sampling.IntervalSamplingConfiguration
import kotlin.test.*


/**
 * Tests for [Smartphone] and [SmartphoneBuilder].
 */
class SmartphoneTest
{
    @Test
    fun builder_sets_sampling_configuration()
    {
        val measureInterval = TimeSpan.fromMinutes( 15.0 )

        val phone = Smartphone( "Test" )
        {
            defaultSamplingConfiguration {
                geolocation { interval = measureInterval }
            }
        }

        val type = Smartphone.SensorsSamplingSchemes.GEOLOCATION.type
        val configuration = phone.defaultSamplingConfiguration[ type ] as IntervalSamplingConfiguration
        assertEquals( measureInterval, configuration.interval )
    }
}
