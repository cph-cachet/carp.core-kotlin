package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.sampling.BatteryAwareSamplingConfiguration
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
import kotlin.test.*


/**
 * Tests for [Smartphone] and [SmartphoneBuilder].
 */
class SmartphoneTest
{
    @Test
    fun builder_sets_fields()
    {
        val expectedGranularity = Granularity.Balanced

        val phone = Smartphone.create( "Test" )
        {
            isOptional = true
            defaultSamplingConfiguration {
                geolocation { allBatteryLevels { granularity = expectedGranularity } }
            }
        }

        assertTrue( phone.isOptional )
        val type = Smartphone.Sensors.GEOLOCATION.dataType.type
        val configuration = phone.defaultSamplingConfiguration[ type ] as? BatteryAwareSamplingConfiguration<*>
        assertNotNull( configuration )
        val configuredGranularity = configuration.normal as GranularitySamplingConfiguration
        assertEquals( expectedGranularity, configuredGranularity.granularity )
    }
}
