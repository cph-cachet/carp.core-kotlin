package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.common.data.STEPCOUNT_TYPE
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for stepcount data, representing the number of steps a participant has taken in a specified time interval.
 */
object Stepcount : DataTypeSamplingScheme<StepcountSamplingConfigurationBuilder>( STEPCOUNT_TYPE )
{
    override fun createSamplingConfigurationBuilder(): StepcountSamplingConfigurationBuilder =
        StepcountSamplingConfigurationBuilder
}

typealias StepcountSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
