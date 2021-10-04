package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.sampling.SamplingConfigurationMapBuilder
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone
 * that collects data which can be incorporated into the platform after it has been processed by a master device (potentially itself).
 * Optionally, a device can present output and receive user input.
 *
 * TODO: Does this also allow specifying dynamic devices? E.g., 'closest smartphone'. Perhaps a 'DeviceSelector'?
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
abstract class DeviceDescriptor<
    TRegistration : DeviceRegistration,
    out TRegistrationBuilder : DeviceRegistrationBuilder<TRegistration>
>
{
    /**
     * A name which describes how the device participates within the study protocol; it's 'role'.
     * E.g., "Patient's phone"
     */
    abstract val roleName: String

    /**
     * The set of [DataType]s defining which data stream data can be collected on this device.
     */
    abstract fun getSupportedDataTypes(): Set<DataType>

    // The following is intentionally `protected` and named `defaultSamplingConfiguration` because:
    // - at runtime, it makes sense defaults defined at compile time are included (see `getDefaultSamplingConfiguration`)
    // - when serialized, it doesn't make sense compile time constants are included, and this is the most logical name
    // - using `@SerialName` to change the serialized name would require all extending classes to do so
    // - at runtime, `getModifiedDefaultSamplingConfigurations` more clearly conveys the difference
    /**
     * Sampling configurations which override the default configurations for data types available on this device.
     * TODO: Verify whether all configured data types are supported by this device (supported data streams), probably in init.
     *       We might also want to check whether the sampling configuration instances are valid.
     */
    protected abstract val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>

    /**
     * Get all sampling configurations which override the default configurations for data types available on this device.
     */
    fun getModifiedDefaultSamplingConfigurations(): Map<DataType, SamplingConfiguration> =
        defaultSamplingConfiguration.toMap()

    /**
     * Do nothing in case [defaultSamplingConfiguration] is valid; throw [IllegalStateException] otherwise.
     * Only known supported data types can be validated; unexpected data types are ignored.
     */
    fun validateDefaultSamplingConfiguration()
    {
        val canBeValidated = getSupportedDataTypes()
        for ( (dataType, samplingConfiguration) in defaultSamplingConfiguration.filter { it.key in canBeValidated } )
        {
            val samplingScheme = checkNotNull( getDataTypeSamplingSchemes()[ dataType ] )
            check( samplingScheme.isValid( samplingConfiguration ) )
                { "The sampling configuration for data type `$dataType` is invalid." }
        }
    }

    /**
     * Get the default sampling configuration to be used for measurements of [dataType],
     * as determined by a configuration overriding the default ([defaultSamplingConfiguration]), or if not present,
     * the default for this [dataType] defined by this device's sampling schemes ([getDataTypeSamplingSchemes]).
     * The configuration to use may still be overridden by individual data stream measures.
     */
    fun getDefaultSamplingConfiguration( dataType: DataType ): SamplingConfiguration =
        defaultSamplingConfiguration[ dataType ]
            ?: getDataTypeSamplingSchemes()[ dataType ]?.default
            ?: throw IllegalArgumentException( "Data type `$dataType` is not supported on this device." )

    /**
     * Return sampling schemes for all the sensors available on this device.
     *
     * Implementations of [DeviceDescriptor] should simply return the mandatory inner object
     * `object Sensors : DataTypeSamplingSchemeMap()` here.
     */
    protected abstract fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap

    protected abstract fun createDeviceRegistrationBuilder(): TRegistrationBuilder

    /**
     * Create a [DeviceRegistration] which can be used to configure this device for deployment.
     * Use [builder] to configure device-specific registration options, if any.
     */
    fun createRegistration( builder: TRegistrationBuilder.() -> Unit = {} ): TRegistration =
        createDeviceRegistrationBuilder().apply( builder ).build()

    /**
     * Return the class information of the [DeviceRegistration] class used to register devices for this [DeviceDescriptor].
     */
    abstract fun getRegistrationClass(): KClass<TRegistration>

    /**
     * Determines whether the device specifications defined in [registration]
     * live up to the minimum specifications defined by the researcher in this [DeviceDescriptor].
     */
    abstract fun isValidRegistration( registration: TRegistration ): Trilean

    /**
     * Verify whether the passed registration is known to be invalid for the given device.
     * In case this is unknown since the the device type is not known at runtime, false is returned.
     */
    fun isDefinitelyInvalidRegistration( registration: DeviceRegistration ): Boolean
    {
        // TODO: `getRegistrationClass` is a trivial implementation in extending classes, but could this be enforced by using the type system instead?
        //  On the JVM runtime, `isValidRegistration` throws a `ClassCastException` when the wrong type were to be passed, but not on JS runtime.
        val isValidType = getRegistrationClass().isInstance( registration )

        @Suppress( "UNCHECKED_CAST" )
        val anyDevice = this as DeviceDescriptor<DeviceRegistration, *>

        return !isValidType || ( anyDevice.isValidRegistration( registration ) == Trilean.FALSE )
    }
}

typealias AnyDeviceDescriptor = DeviceDescriptor<*, *>
typealias DeviceType = KClass<out AnyDeviceDescriptor>


/**
 * A helper class to configure and construct immutable [DeviceDescriptor] classes.
 */
@DeviceDescriptorBuilderDsl
abstract class DeviceDescriptorBuilder<TSamplingConfigurationMapBuilder : SamplingConfigurationMapBuilder>
{
    private var samplingConfigurationBuilder: TSamplingConfigurationMapBuilder.() -> Unit = { }

    /**
     * Override default sampling configurations for data types available on this device.
     */
    fun defaultSamplingConfiguration( builder: TSamplingConfigurationMapBuilder.() -> Unit )
    {
        samplingConfigurationBuilder = builder
    }

    protected abstract fun createSamplingConfigurationMapBuilder(): TSamplingConfigurationMapBuilder

    fun buildSamplingConfiguration(): Map<DataType, SamplingConfiguration> =
        createSamplingConfigurationMapBuilder().apply( samplingConfigurationBuilder ).build()
}

/**
 * Should be applied to all builders participating in building [DeviceDescriptor]s to prevent misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class DeviceDescriptorBuilderDsl
