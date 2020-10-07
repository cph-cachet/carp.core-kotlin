package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for stepcount data, representing the number of steps a participant has taken in a specified time interval.
 */
object StepcountSamplingScheme : DataTypeSamplingScheme<StepcountSamplingConfigurationBuilder>( CarpDataTypes.STEPCOUNT )
{
    override fun createSamplingConfigurationBuilder(): StepcountSamplingConfigurationBuilder =
        StepcountSamplingConfigurationBuilder
}

typealias StepcountSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
