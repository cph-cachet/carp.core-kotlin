package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorBuilder
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


/**
 * A base class which can be used by [DeviceDescriptorBuilder]s to initialize sampling configurations for all data types available on the device.
 */
@DeviceDescriptorBuilderDsl
open class SamplingConfigurationMapBuilder
{
    private val samplingConfigurations: MutableMap<DataType, SamplingConfiguration> = mutableMapOf()

    protected fun <TBuilder : SamplingConfigurationBuilder> addConfiguration(
        dataType: DataTypeMetadata<TBuilder>,
        builder: TBuilder.() -> Unit
    ): SamplingConfiguration
    {
        val configuration = dataType.samplingConfiguration( builder )
        samplingConfigurations[ dataType.TYPE ] = configuration
        return configuration
    }

    fun build(): Map<DataType, SamplingConfiguration> = samplingConfigurations.toMap()
}
