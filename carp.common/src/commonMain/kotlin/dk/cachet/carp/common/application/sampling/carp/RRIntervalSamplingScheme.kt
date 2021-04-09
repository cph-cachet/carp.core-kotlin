package dk.cachet.carp.common.application.sampling.carp

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingScheme
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfigurationBuilder


object RRIntervalSamplingScheme : DataTypeSamplingScheme<RRIntervalSamplingConfigurationBuilder>( CarpDataTypes.RR_INTERVAL )
{
    override fun createSamplingConfigurationBuilder(): RRIntervalSamplingConfigurationBuilder =
        RRIntervalSamplingConfigurationBuilder
}

typealias RRIntervalSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
