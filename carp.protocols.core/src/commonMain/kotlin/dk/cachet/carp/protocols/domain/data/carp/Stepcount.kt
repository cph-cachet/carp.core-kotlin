package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for stepcount data, representing the number of steps a participant has taken in a specified time interval.
 */
object Stepcount : DataTypeSamplingScheme<StepcountSamplingConfigurationBuilder>( carpDataType( "stepcount" ) )
{
    override fun createSamplingConfigurationBuilder(): StepcountSamplingConfigurationBuilder =
        StepcountSamplingConfigurationBuilder
}

typealias StepcountSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
