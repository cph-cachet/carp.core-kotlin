@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import dk.cachet.carp.common.infrastructure.test.StubSamplingConfiguration
import dk.cachet.carp.common.infrastructure.test.StubSamplingConfigurationBuilder
import kotlin.test.*
import kotlin.time.Duration.Companion.minutes


/**
 * Tests for [BatteryAwareSamplingScheme].
 */
class BatteryAwareSamplingSchemeTest
{
    class TestBatteryAwareSamplingScheme :
        BatteryAwareSamplingScheme<StubSamplingConfiguration, StubSamplingConfigurationBuilder>(
            StubDataTypes.STUB_POINT,
            { StubSamplingConfigurationBuilder( "Test" ) },
            StubSamplingConfiguration( "Normal" ),
            StubSamplingConfiguration( "Low" )
        )
    {
        override fun isValidBatteryLevelConfiguration( configuration: StubSamplingConfiguration ): Boolean =
            configuration.configuration != "Invalid"
    }

    @Test
    fun isValid_true_for_correct_sampling_configuration_types()
    {
        val scheme = TestBatteryAwareSamplingScheme()

        val validConfiguration = BatteryAwareSamplingConfiguration(
            normal = StubSamplingConfiguration( "Balanced" ),
            low = StubSamplingConfiguration( "Coarse" ),
            critical = StubSamplingConfiguration( "Off" )
        )
        assertTrue( scheme.isValid( validConfiguration ) )
    }

    @Test
    fun isValid_true_when_critical_configurations_is_not_set()
    {
        val scheme = TestBatteryAwareSamplingScheme()

        val validConfiguration = BatteryAwareSamplingConfiguration(
            normal = StubSamplingConfiguration( "Balanced" ),
            low = StubSamplingConfiguration( "Low" )
        )
        assertTrue( scheme.isValid( validConfiguration ) )
    }

    @Test
    fun isValid_false_when_incorrect_sampling_configuration_type_is_included()
    {
        val scheme = TestBatteryAwareSamplingScheme()

        val invalidConfiguration = BatteryAwareSamplingConfiguration(
            normal = StubSamplingConfiguration( "Balanced" ),
            low = IntervalSamplingConfiguration( 1.minutes ),
            critical = StubSamplingConfiguration( "Off" )
        )
        assertFalse( scheme.isValid( invalidConfiguration ) )
    }

    @Test
    fun isValid_false_when_constraints_are_not_met()
    {
        val scheme = TestBatteryAwareSamplingScheme()

        // "Invalid" breaks the constraint implemented in `TestBatteryAwareSamplingScheme`.
        val invalidConfiguration = BatteryAwareSamplingConfiguration(
            normal = StubSamplingConfiguration( "Invalid" ),
            low = StubSamplingConfiguration( "Low" )
        )
        assertFalse( scheme.isValid( invalidConfiguration ) )
    }

    @Test
    fun samplingConfiguration_succeeds()
    {
        val scheme = TestBatteryAwareSamplingScheme()

        val expectedConfiguration = "Expected"
        val expected = StubSamplingConfiguration( expectedConfiguration )
        assertBatteryAwareSamplingConfiguration( expected, expected, expected )
        {
            scheme.samplingConfiguration {
                batteryNormal { configuration = expectedConfiguration }
                batteryLow { configuration = expectedConfiguration }
                batteryCritical { configuration = expectedConfiguration }
            }
        }
    }

    @Test
    fun samplingConfiguration_allBatteryLevels_succeeds()
    {
        val scheme = TestBatteryAwareSamplingScheme()

        val expectedConfiguration = "Expected"
        val expected = StubSamplingConfiguration( expectedConfiguration )
        assertBatteryAwareSamplingConfiguration( expected, expected, expected ) {
            scheme.samplingConfiguration { allBatteryLevels { configuration = expectedConfiguration } }
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
