@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.devices.DeviceDescriptor


/**
 * Sampling scheme which provides only indirect control over how data is sampled by specifying a desired level of [Granularity].
 * The levels of granularity correspond to expected degrees of power consumption.
 * By default, [Granularity.Balanced] is used; when the battery is low, [Granularity.Coarse]; when battery is critically low, sampling stops.
 */
class AdaptiveGranularitySamplingScheme( dataType: DataTypeMetaData ) :
    BatteryAwareSamplingScheme<GranularitySamplingConfiguration, GranularitySamplingConfigurationBuilder>(
        dataType,
        { GranularitySamplingConfigurationBuilder( Granularity.Balanced ) },
        normal = GranularitySamplingConfiguration( Granularity.Balanced ),
        low = GranularitySamplingConfiguration( Granularity.Coarse )
    )
{
    override fun isValidBatteryLevelConfiguration( configuration: GranularitySamplingConfiguration ): Boolean = true
}

/**
 * A helper class to configure and construct immutable [BatteryAwareSamplingConfiguration] objects
 * using [GranularitySamplingConfiguration] as part of setting up a [DeviceDescriptor].
 */
typealias AdaptiveGranularitySamplingConfigurationBuilder =
    BatteryAwareSamplingConfigurationBuilder<
        GranularitySamplingConfiguration,
        GranularitySamplingConfigurationBuilder
    >
