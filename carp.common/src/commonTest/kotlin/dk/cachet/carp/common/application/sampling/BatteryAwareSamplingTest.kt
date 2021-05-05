@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import kotlin.test.*


/**
 * Tests for [BatteryAwareSamplingScheme].
 */
class BatteryAwareSamplingSchemeTest
{
    @Test
    fun isValid_true_for_correct_sampling_configuration_types()
    {
        val scheme = BatteryAwareSamplingScheme(
            STUB_DATA_TYPE,
            builder = { GranularitySamplingConfigurationBuilder( Granularity.Balanced ) },
            normal = GranularitySamplingConfiguration( Granularity.Balanced )
        )

        val validConfiguration = BatteryAwareSamplingConfiguration(
            normal = GranularitySamplingConfiguration( Granularity.Balanced ),
            low = GranularitySamplingConfiguration( Granularity.Coarse ),
            critical = GranularitySamplingConfiguration( Granularity.Coarse )
        )
        assertTrue( scheme.isValid( validConfiguration ) )
    }

    @Test
    fun isValid_true_when_some_configurations_are_not_set()
    {
        val scheme = BatteryAwareSamplingScheme(
            STUB_DATA_TYPE,
            builder = { GranularitySamplingConfigurationBuilder( Granularity.Balanced ) },
            normal = GranularitySamplingConfiguration( Granularity.Balanced )
        )

        val validConfiguration = BatteryAwareSamplingConfiguration(
            normal = GranularitySamplingConfiguration( Granularity.Balanced )
        )
        assertTrue( scheme.isValid( validConfiguration ) )
    }

    @Test
    fun isValid_false_when_incorrect_sampling_configuration_type_is_included()
    {
        val scheme = BatteryAwareSamplingScheme(
            STUB_DATA_TYPE,
            builder = { GranularitySamplingConfigurationBuilder( Granularity.Balanced ) },
            normal = GranularitySamplingConfiguration( Granularity.Balanced )
        )

        val invalidConfiguration = BatteryAwareSamplingConfiguration(
            normal = GranularitySamplingConfiguration( Granularity.Balanced ),
            low = IntervalSamplingConfiguration( TimeSpan.fromMinutes( 1.0 ) ),
            critical = GranularitySamplingConfiguration( Granularity.Coarse )
        )
        assertFalse( scheme.isValid( invalidConfiguration ) )
    }

    @Test
    fun samplingConfiguration_succeeds()
    {
        val scheme = BatteryAwareSamplingScheme(
            STUB_DATA_TYPE,
            builder = { GranularitySamplingConfigurationBuilder( Granularity.Balanced ) },
            normal = GranularitySamplingConfiguration( Granularity.Balanced )
        )

        val expectedGranularity = Granularity.Coarse
        val expected = GranularitySamplingConfiguration( expectedGranularity )
        assertBatteryAwareSamplingConfiguration( expected, expected, expected )
        {
            scheme.samplingConfiguration {
                batteryNormal { granularity = expectedGranularity }
                batteryLow { granularity = expectedGranularity }
                batteryCritical { granularity = expectedGranularity }
            }
        }
    }

    @Test
    fun samplingConfiguration_allBatteryLevels_succeeds()
    {
        val scheme = BatteryAwareSamplingScheme(
            STUB_DATA_TYPE,
            builder = { GranularitySamplingConfigurationBuilder( Granularity.Balanced ) },
            normal = GranularitySamplingConfiguration( Granularity.Balanced )
        )

        val expectedGranularity = Granularity.Coarse
        val expected = GranularitySamplingConfiguration( expectedGranularity )
        assertBatteryAwareSamplingConfiguration( expected, expected, expected ) {
            scheme.samplingConfiguration { allBatteryLevels { granularity = expectedGranularity } }
        }
    }

    private fun assertBatteryAwareSamplingConfiguration(
        expectedNormal: SamplingConfiguration,
        expectedLow: SamplingConfiguration,
        expectedCritical: SamplingConfiguration,
        createActualConfiguration: () -> SamplingConfiguration
    )
    {
        val actual = createActualConfiguration()

        assertTrue( actual is BatteryAwareSamplingConfiguration<*> )
        val normal = actual.normal
        val low = actual.low
        val critical = actual.critical
        assertEquals( expectedNormal, normal )
        assertEquals( expectedLow, low )
        assertEquals( expectedCritical, critical )
    }
}
