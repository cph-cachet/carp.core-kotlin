package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorBuilderDsl
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * Contains configuration on how to sample a data stream of a given type.
 */
@Serializable
@Polymorphic
abstract class SamplingConfiguration : Immutable( notImmutableErrorFor( SamplingConfiguration::class ) )


/**
 * A helper class to configure and construct immutable [SamplingConfiguration] classes
 * as part of setting up a [DeviceDescriptor].
*/
@DeviceDescriptorBuilderDsl
interface SamplingConfigurationBuilder
{
    /**
     * Build the immutable [SamplingConfiguration] using the current configuration of this [SamplingConfigurationBuilder].
     */
    fun build(): SamplingConfiguration
}
