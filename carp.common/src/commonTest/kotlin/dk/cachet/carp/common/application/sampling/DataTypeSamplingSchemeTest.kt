package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE_METADATA
import kotlin.test.*


/**
 * Tests for [DataTypeSamplingScheme].
 */
class DataTypeSamplingSchemeTest
{
    class TestSamplingScheme : DataTypeSamplingScheme<IntervalSamplingConfigurationBuilder>( STUB_DATA_TYPE_METADATA )
    {
        val maxSeconds: Double = 42.0

        override fun createSamplingConfigurationBuilder(): IntervalSamplingConfigurationBuilder =
            IntervalSamplingConfigurationBuilder( TimeSpan.fromSeconds( 5.0 ) )

        override fun isValid( configuration: SamplingConfiguration ): Boolean =
            configuration is IntervalSamplingConfiguration && configuration.interval.totalSeconds <= maxSeconds
    }


    @Test
    fun samplingConfiguration_fails_when_constraints_are_broken()
    {
        val scheme = TestSamplingScheme()

        val exceedInterval = TimeSpan.fromSeconds( scheme.maxSeconds + 1 )
        assertFailsWith<IllegalArgumentException> { scheme.measure { interval = exceedInterval } }
    }
}
