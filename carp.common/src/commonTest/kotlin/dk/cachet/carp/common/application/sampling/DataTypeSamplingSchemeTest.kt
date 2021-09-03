package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import kotlin.test.*
import kotlin.time.Duration


/**
 * Tests for [DataTypeSamplingScheme].
 */
class DataTypeSamplingSchemeTest
{
    class TestSamplingScheme : DataTypeSamplingScheme<IntervalSamplingConfigurationBuilder>( StubDataTypes.STUB )
    {
        val maxDuration: Duration = Duration.seconds( 42 )

        override fun createSamplingConfigurationBuilder(): IntervalSamplingConfigurationBuilder =
            IntervalSamplingConfigurationBuilder( Duration.seconds( 5 ) )

        override fun isValid( configuration: SamplingConfiguration ): Boolean =
            configuration is IntervalSamplingConfiguration && configuration.interval <= maxDuration
    }


    @Test
    fun samplingConfiguration_fails_when_constraints_are_broken()
    {
        val scheme = TestSamplingScheme()

        val exceedInterval = scheme.maxDuration + Duration.seconds( 1 )
        assertFailsWith<IllegalArgumentException> { scheme.measure { interval = exceedInterval } }
    }
}
