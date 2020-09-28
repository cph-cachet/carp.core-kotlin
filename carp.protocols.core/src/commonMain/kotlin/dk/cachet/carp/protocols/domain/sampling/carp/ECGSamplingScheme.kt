package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.data.ECG_TYPE
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.IntervalSamplingConfigurationBuilder

class ECGSamplingScheme(
    val defaultMeasureInterval: TimeSpan
) : DataTypeSamplingScheme<ECGSamplingConfigurationBuilder>( ECG_TYPE ){
    override fun createSamplingConfigurationBuilder(): ECGSamplingConfigurationBuilder =
        ECGSamplingConfigurationBuilder( defaultMeasureInterval )
}

typealias ECGSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder