package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Sampling scheme which allows configuring a time interval in between subsequent measurements.
 */
class IntervalSamplingScheme( dataType: DataType, val defaultMeasureInterval: TimeSpan ) :
    DataTypeSamplingScheme<IntervalSamplingConfigurationBuilder>( dataType )
{
    override fun createSamplingConfigurationBuilder(): IntervalSamplingConfigurationBuilder =
        IntervalSamplingConfigurationBuilder( defaultMeasureInterval )

    override fun isValid( configuration: SamplingConfiguration ) = configuration is IntervalSamplingConfiguration
}


/**
 * A sampling configuration which allows configuring the time [interval] in between subsequent measurements.
 */
@Serializable
data class IntervalSamplingConfiguration( val interval: TimeSpan ) : SamplingConfiguration


/**
 * A helper class to configure and construct immutable [IntervalSamplingConfiguration] objects
 * as part of setting up a [DeviceDescriptor].
 */
class IntervalSamplingConfigurationBuilder( var interval: TimeSpan ) :
    SamplingConfigurationBuilder<IntervalSamplingConfiguration>
{
    override fun build(): IntervalSamplingConfiguration = IntervalSamplingConfiguration( interval )
}
