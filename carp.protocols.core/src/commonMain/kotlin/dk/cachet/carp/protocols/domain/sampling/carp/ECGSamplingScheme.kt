package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.ECG_TYPE
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for ECG, representing electrical activity of the heart over time for a single lead.
 */
object ECGSamplingScheme : DataTypeSamplingScheme<ECGSamplingConfigurationBuilder>( ECG_TYPE )
{
    override fun createSamplingConfigurationBuilder(): ECGSamplingConfigurationBuilder =
        ECGSamplingConfigurationBuilder
}

typealias ECGSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
