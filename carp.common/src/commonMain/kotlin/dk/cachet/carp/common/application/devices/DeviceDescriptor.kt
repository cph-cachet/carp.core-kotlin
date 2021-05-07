package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
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
     * The set of [DataType]s defining which data can be collected on this device.
     */
    abstract val supportedDataTypes: Set<DataType>

    /**
     * Sampling configurations which override the default configurations for data types available on this device.
     * TODO: Verify whether all configured data types are supported by this device (supported data streams), probably in init.
     *       We might also want to check whether the sampling configuration instances are valid.
     */
    abstract val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>

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
     * Determines whether the given [registration] is configured correctly for this type of device.
     * Devices rely on a concrete [DeviceRegistration] to determine the specific configuration needed for them.
     */
    abstract fun isValidConfiguration( registration: TRegistration ): Trilean
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
