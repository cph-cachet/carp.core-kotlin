package dk.cachet.carp.common.application.sampling.carp

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingScheme
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfigurationBuilder


object SensorSkinContactSamplingScheme : DataTypeSamplingScheme<SensorSkinContactSamplingConfigurationBuilder>( CarpDataTypes.SENSOR_SKIN_CONTACT )
{
    override fun createSamplingConfigurationBuilder(): SensorSkinContactSamplingConfigurationBuilder =
        SensorSkinContactSamplingConfigurationBuilder
}

typealias SensorSkinContactSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
