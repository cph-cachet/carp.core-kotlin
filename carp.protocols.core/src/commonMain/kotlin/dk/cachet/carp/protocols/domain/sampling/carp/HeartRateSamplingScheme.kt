package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.HEARTRATE_TYPE
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder

class HeartRateSamplingScheme : DataTypeSamplingScheme<HearRateSamplingConfigurationBuilder>( HEARTRATE_TYPE ) {
    override fun createSamplingConfigurationBuilder(): HearRateSamplingConfigurationBuilder =
        HearRateSamplingConfigurationBuilder
}

typealias HearRateSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder