package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder


object RRIntervalSamplingScheme : DataTypeSamplingScheme<RRIntervalSamplingConfigurationBuilder>( CarpDataTypes.RR_INTERVAL )
{
    override fun createSamplingConfigurationBuilder(): RRIntervalSamplingConfigurationBuilder =
        RRIntervalSamplingConfigurationBuilder
}

typealias RRIntervalSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
