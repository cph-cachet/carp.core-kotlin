package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * A sampling configuration which allows configuring the time [interval] in between subsequent measurements.
 */
@Serializable
data class IntervalSamplingConfiguration( val interval: TimeSpan ) : SamplingConfiguration


/**
 * A helper class to configure and construct immutable [IntervalSamplingConfiguration] classes
 * as part of setting up a [DeviceDescriptor].
 */
class IntervalSamplingConfigurationBuilder( var interval: TimeSpan ) : SamplingConfigurationBuilder
{
    override fun build(): IntervalSamplingConfiguration = IntervalSamplingConfiguration( interval )
}
