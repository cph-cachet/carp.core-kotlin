package dk.cachet.carp.common.application.sampling.carp

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingScheme
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for heart rate data, representing the number of heart contractions (beats) per minute.
 */
object HeartRateSamplingScheme : DataTypeSamplingScheme<HeartRateSamplingConfigurationBuilder>( CarpDataTypes.HEART_RATE )
{
    override fun createSamplingConfigurationBuilder(): HeartRateSamplingConfigurationBuilder =
        HeartRateSamplingConfigurationBuilder
}

typealias HeartRateSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
