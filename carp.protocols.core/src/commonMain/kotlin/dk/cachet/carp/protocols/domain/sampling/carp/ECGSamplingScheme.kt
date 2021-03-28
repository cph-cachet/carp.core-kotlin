package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for ECG, representing electrical activity of the heart over time for a single lead.
 */
object ECGSamplingScheme : DataTypeSamplingScheme<ECGSamplingConfigurationBuilder>( CarpDataTypes.ECG )
{
    override fun createSamplingConfigurationBuilder(): ECGSamplingConfigurationBuilder =
        ECGSamplingConfigurationBuilder
}

typealias ECGSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
