package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfiguration
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
            samplingConfiguration {
                geolocation { interval = measureInterval }
            }
        }

        val type = Smartphone.SensorsSamplingSchemes.GEOLOCATION.type
        val configuration = phone.samplingConfiguration[ type ] as IntervalSamplingConfiguration
        assertEquals( measureInterval, configuration.interval )
    }
}
