package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.NoOptionsSamplingConfigurationBuilder


/**
 * Sampling scheme for heart rate data, representing the number of heart contractions (beats) per minute.
 */
object SensorSkinContactSamplingScheme : DataTypeSamplingScheme<SensorSkinContactSamplingConfigurationBuilder>( CarpDataTypes.SENSOR_SKIN_CONTACT )
{
    override fun createSamplingConfigurationBuilder(): SensorSkinContactSamplingConfigurationBuilder =
        SensorSkinContactSamplingConfigurationBuilder
}

typealias SensorSkinContactSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
