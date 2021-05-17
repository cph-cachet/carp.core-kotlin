package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Sampling scheme which provides only indirect control over how data is sampled by specifying a desired level of [Granularity].
 * The levels of granularity correspond to expected degrees of power consumption.
 */
class GranularitySamplingScheme( dataType: DataType, val defaultGranularity: Granularity ) :
    DataTypeSamplingScheme<GranularitySamplingConfigurationBuilder>( dataType )
{
    override fun createSamplingConfigurationBuilder(): GranularitySamplingConfigurationBuilder =
        GranularitySamplingConfigurationBuilder( defaultGranularity )

    override fun isValid( configuration: SamplingConfiguration ) = configuration is GranularitySamplingConfiguration
}


/**
 * The level of detail a data stream should be sampled at, corresponding to expected degrees of power consumption.
 */
@Serializable
enum class Granularity
{
    /**
     * Consumes a lot of power. Only use for short periods of time or when power consumption is not an issue.
     */
    Detailed,

    /**
     * Balanced power consumption. For battery-based devices this aims not to require more than one recharge per day.
     */
    Balanced,

    /**
     * Minimal impact on power consumption, but only provides a very coarse level of detail.
     */
    Coarse
}


/**
 * A [SamplingConfiguration] which allows specifying a desired level of [granularity],
 * corresponding to expected degrees of power consumption.
 */
@Serializable
data class GranularitySamplingConfiguration( val granularity: Granularity ) : SamplingConfiguration


/**
 * A helper class to configure and construct immutable [GranularitySamplingConfiguration] objects
 * as part of setting up a [DeviceDescriptor].
 */
class GranularitySamplingConfigurationBuilder( var granularity: Granularity ) :
    SamplingConfigurationBuilder<GranularitySamplingConfiguration>
{
    override fun build(): GranularitySamplingConfiguration = GranularitySamplingConfiguration( granularity )
}
