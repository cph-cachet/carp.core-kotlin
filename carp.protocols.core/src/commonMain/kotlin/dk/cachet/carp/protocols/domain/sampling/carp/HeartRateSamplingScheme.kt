package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.data.HEARTRATE_TYPE
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.IntervalSamplingConfigurationBuilder

class HeartRateSamplingScheme(
    val defaultSamplingInterval: TimeSpan
): DataTypeSamplingScheme<HearRateSamplingConfigurationBuilder>( HEARTRATE_TYPE ) {
    override fun createSamplingConfigurationBuilder(): HearRateSamplingConfigurationBuilder =
        HearRateSamplingConfigurationBuilder( defaultSamplingInterval )
}

typealias HearRateSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder