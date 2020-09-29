package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.ECG_TYPE
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder

class ECGSamplingScheme : DataTypeSamplingScheme<ECGSamplingConfigurationBuilder>( ECG_TYPE ){
    override fun createSamplingConfigurationBuilder(): ECGSamplingConfigurationBuilder =
        ECGSamplingConfigurationBuilder
}

typealias ECGSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder