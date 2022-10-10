package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * Tests for [DataTypeSamplingScheme].
 */
class DataTypeSamplingSchemeTest
{
    class TestSamplingScheme : DataTypeSamplingScheme<IntervalSamplingConfigurationBuilder>(
        StubDataTypes.STUB_POINT,
        IntervalSamplingConfiguration( 1.seconds )
    )
    {
        val maxDuration: Duration = 42.seconds

        override fun createSamplingConfigurationBuilder(): IntervalSamplingConfigurationBuilder =
            IntervalSamplingConfigurationBuilder( 5.seconds, null )

        override fun isValid( configuration: SamplingConfiguration ): Boolean =
            configuration is IntervalSamplingConfiguration && configuration.interval <= maxDuration
    }


    @Test
    fun samplingConfiguration_fails_when_constraints_are_broken()
    {
        val scheme = TestSamplingScheme()

        val exceedInterval = scheme.maxDuration + 1.seconds
        assertFailsWith<IllegalArgumentException> { scheme.measure { interval = exceedInterval } }
    }
}
