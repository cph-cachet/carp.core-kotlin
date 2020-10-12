package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for step count data, representing the number of steps a participant has taken in a specified time interval.
 */
object StepCountSamplingScheme : DataTypeSamplingScheme<StepCountSamplingConfigurationBuilder>( CarpDataTypes.STEP_COUNT )
{
    override fun createSamplingConfigurationBuilder(): StepCountSamplingConfigurationBuilder =
        StepCountSamplingConfigurationBuilder
}

typealias StepCountSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
