package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A sampling scheme which changes based on how much battery the device has left.
 */
abstract class BatteryAwareSamplingScheme<
    TConfiguration : SamplingConfiguration,
    TBuilder : SamplingConfigurationBuilder<TConfiguration>
>(
    dataType: DataType,
    /**
     * The builder to construct [SamplingConfiguration]s when overriding configurations for different battery levels.
     */
    private val builder: () -> TBuilder,
    /**
     * The default sampling configuration to use when there is plenty of battery left.
     */
    private val normal: TConfiguration,
    /**
     * The default sampling configuration to use when the battery is low.
     */
    private val low: TConfiguration? = null,
    /**
     * The default sampling configuration to use when the battery is critically low.
     * By default, sampling should be disabled at this point.
     */
    private val critical: TConfiguration? = null
) : DataTypeSamplingScheme<BatteryAwareSamplingConfigurationBuilder<TConfiguration, TBuilder>>( dataType )
{
    private val configurationKlass: KClass<out TConfiguration> = normal::class


    override fun createSamplingConfigurationBuilder(): BatteryAwareSamplingConfigurationBuilder<TConfiguration, TBuilder> =
        BatteryAwareSamplingConfigurationBuilder( builder, normal, low, critical )

    override fun isValid( configuration: SamplingConfiguration ): Boolean
    {
        if ( configuration !is BatteryAwareSamplingConfiguration<*> ) return false

        // Verify whether battery-level-specific configurations are the expected type.
        val correctTypes =
            configurationKlass.isInstance( configuration.normal ) && // Normal configuration cannot be null.
            ( configuration.low == null || configurationKlass.isInstance( configuration.low ) ) &&
            ( configuration.critical == null || configurationKlass.isInstance( configuration.critical ) )
        if ( !correctTypes ) return false

        // Verify whether constraints for the battery-level-specific configurations are met.
        @Suppress( "UNCHECKED_CAST" )
        return isValidBatteryLevelConfiguration( configuration.normal as TConfiguration ) &&
            ( configuration.low == null || isValidBatteryLevelConfiguration( configuration.low as TConfiguration ) ) &&
            ( configuration.critical == null || isValidBatteryLevelConfiguration( configuration.critical as TConfiguration ) )
    }

    /**
     * Determines whether the [configuration] assigned to a specific battery level in a [BatteryAwareSamplingConfiguration]
     * is valid for the constraints defined in this sampling scheme.
     */
    abstract fun isValidBatteryLevelConfiguration( configuration: TConfiguration ): Boolean
}


/**
 * A sampling configuration which changes based on how much battery the device has left.
 */
@Serializable
data class BatteryAwareSamplingConfiguration<TConfiguration : SamplingConfiguration>(
    /**
     * The sampling configuration to use when there is plenty of battery left.
     */
    val normal: TConfiguration,
    /**
     * The sampling configuration to use when the battery is low.
     */
    val low: TConfiguration? = null,
    /**
     * The sampling configuration to use when the battery is critically low.
     */
    val critical: TConfiguration? = null
) : SamplingConfiguration


/**
 * A helper class to configure and construct immutable [BatteryAwareSamplingConfiguration] classes
 * as part of setting up a [DeviceDescriptor].
 */
class BatteryAwareSamplingConfigurationBuilder<
    TConfiguration : SamplingConfiguration,
    TBuilder : SamplingConfigurationBuilder<TConfiguration>
>(
    private val createBuilder: () -> TBuilder,
    private var normal: TConfiguration,
    private var low: TConfiguration?,
    private var critical: TConfiguration?
) : SamplingConfigurationBuilder<BatteryAwareSamplingConfiguration<TConfiguration>>
{
    /**
     * The sampling configuration to use when there is plenty of battery left.
     */
    fun batteryNormal( builder: TBuilder.() -> Unit ) =
        createConfiguration( builder ).let { normal = it }

    /**
     * The sampling configuration to use when the battery is low.
     */
    fun batteryLow( builder: TBuilder.() -> Unit ) =
        createConfiguration( builder ).let { low = it }

    /**
     * The sampling configuration to use when the battery is critically low.
     */
    fun batteryCritical( builder: TBuilder.() -> Unit ) =
        createConfiguration( builder ).let { critical = it }

    /**
     * Apply the same sampling configuration for all battery levels: normal, low, and critical.
     */
    fun allBatteryLevels( builder: TBuilder.() -> Unit ) =
        createConfiguration( builder ).let {
            normal = it
            low = it
            critical = it
        }

    private fun createConfiguration( builder: TBuilder.() -> Unit ) =
        createBuilder().apply( builder ).build()

    override fun build(): BatteryAwareSamplingConfiguration<TConfiguration> =
        BatteryAwareSamplingConfiguration( normal, low, critical )
}
