package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * A sampling scheme which changes based on how much battery the device has left.
 */
@JsExport
abstract class BatteryAwareSamplingScheme<
    TConfig : SamplingConfiguration,
    TBuilder : SamplingConfigurationBuilder<TConfig>
>(
    dataType: DataTypeMetaData,
    /**
     * The builder to construct [SamplingConfiguration]s when overriding configurations for different battery levels.
     */
    private val builder: () -> TBuilder,
    /**
     * The default sampling configuration to use when there is plenty of battery left.
     */
    val normal: TConfig,
    /**
     * The default sampling configuration to use when the battery is low.
     */
    val low: TConfig,
    /**
     * The default sampling configuration to use when the battery is critically low.
     * By default, sampling should be disabled at this point.
     */
    val critical: TConfig? = null
) : DataTypeSamplingScheme<BatteryAwareSamplingConfigurationBuilder<TConfig, TBuilder>>(
    dataType,
    BatteryAwareSamplingConfiguration( normal, low, critical )
)
{
    private val configurationKlass: KClass<out TConfig> = normal::class


    override fun createSamplingConfigurationBuilder(): BatteryAwareSamplingConfigurationBuilder<TConfig, TBuilder> =
        BatteryAwareSamplingConfigurationBuilder( builder, normal, low, critical )

    override fun isValid( configuration: SamplingConfiguration ): Boolean
    {
        if ( configuration !is BatteryAwareSamplingConfiguration<*> ) return false

        // Verify whether battery-level-specific configurations are the expected type.
        val correctTypes =
            configurationKlass.isInstance( configuration.normal ) && // Normal configuration cannot be null.
            ( configurationKlass.isInstance( configuration.low ) ) &&
            ( configuration.critical == null || configurationKlass.isInstance( configuration.critical ) )
        if ( !correctTypes ) return false

        // Verify whether constraints for the battery-level-specific configurations are met.
        @Suppress( "UNCHECKED_CAST" )
        return isValidBatteryLevelConfiguration( configuration.normal as TConfig ) &&
            ( isValidBatteryLevelConfiguration( configuration.low as TConfig ) ) &&
            ( configuration.critical == null || isValidBatteryLevelConfiguration( configuration.critical as TConfig ) )
    }

    /**
     * Determines whether the [configuration] assigned to a specific battery level in a [BatteryAwareSamplingConfiguration]
     * is valid for the constraints defined in this sampling scheme.
     */
    abstract fun isValidBatteryLevelConfiguration( configuration: TConfig ): Boolean
}


/**
 * A sampling configuration which changes based on how much battery the device has left.
 */
@Serializable
@JsExport
data class BatteryAwareSamplingConfiguration<TConfig : SamplingConfiguration>(
    /**
     * The sampling configuration to use when there is plenty of battery left.
     */
    val normal: TConfig,
    /**
     * The sampling configuration to use when the battery is low.
     */
    val low: TConfig,
    /**
     * The sampling configuration to use when the battery is critically low.
     */
    val critical: TConfig? = null
) : SamplingConfiguration


/**
 * A helper class to configure and construct immutable [BatteryAwareSamplingConfiguration] classes
 * as part of setting up a [DeviceConfiguration].
 */
class BatteryAwareSamplingConfigurationBuilder<
    TConfig : SamplingConfiguration,
    TBuilder : SamplingConfigurationBuilder<TConfig>
>(
    private val createBuilder: () -> TBuilder,
    private var normal: TConfig,
    private var low: TConfig,
    private var critical: TConfig?
) : SamplingConfigurationBuilder<BatteryAwareSamplingConfiguration<TConfig>>
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

    override fun build(): BatteryAwareSamplingConfiguration<TConfig> =
        BatteryAwareSamplingConfiguration( normal, low, critical )
}
