package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceConfigurationBuilder
import dk.cachet.carp.common.application.devices.DeviceConfigurationBuilderDsl
import kotlinx.serialization.Polymorphic
import kotlin.js.JsExport


/**
 * Contains configuration on how to sample a data stream of a given type.
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
@JsExport
interface SamplingConfiguration


/**
 * A helper class to configure and construct immutable [SamplingConfiguration] classes
 * as part of setting up a [DeviceConfiguration].
*/
@DeviceConfigurationBuilderDsl
interface SamplingConfigurationBuilder<TConfig : SamplingConfiguration>
{
    /**
     * Build the immutable [SamplingConfiguration] using the current configuration of this [SamplingConfigurationBuilder]
     * and verify whether the constraints specified in [samplingScheme] are followed.
     *
     * @throws IllegalArgumentException when the constructed sampling configuration breaks constraints specified in [samplingScheme].
     */
    fun build( samplingScheme: DataTypeSamplingScheme<*> ): TConfig
    {
        val configuration = build()
        require( samplingScheme.isValid( configuration ) )
            { "The configured sampling configuration is invalid for the corresponding sampling scheme." }

        return configuration
    }

    /**
     * Build the immutable [SamplingConfiguration] using the current configuration of this [SamplingConfigurationBuilder].
     */
    fun build(): TConfig
}


/**
 * A base class which can be used by [DeviceConfigurationBuilder]s to initialize sampling configurations for all data types available on the device.
 */
@DeviceConfigurationBuilderDsl
open class SamplingConfigurationMapBuilder
{
    private val samplingConfigurations: MutableMap<DataType, SamplingConfiguration> = mutableMapOf()

    protected fun <TBuilder : SamplingConfigurationBuilder<*>> addConfiguration(
        samplingScheme: DataTypeSamplingScheme<TBuilder>,
        builder: TBuilder.() -> Unit
    ): SamplingConfiguration
    {
        val configuration = samplingScheme.samplingConfiguration( builder )
        samplingConfigurations[ samplingScheme.dataType.type ] = configuration
        return configuration
    }

    fun build(): Map<DataType, SamplingConfiguration> = samplingConfigurations.toMap()
}
