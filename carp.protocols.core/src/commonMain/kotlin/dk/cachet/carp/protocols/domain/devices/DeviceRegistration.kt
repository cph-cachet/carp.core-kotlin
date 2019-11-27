package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.*


/**
 * A [DeviceRegistration] configures a [DeviceDescriptor] as part of the deployment of a [StudyProtocol].
 */
@Serializable
@Polymorphic
abstract class DeviceRegistration : Immutable( notImmutableErrorFor( DeviceRegistration::class ) )
{
    /**
     * An ID for the device, used to disambiguate between devices of the same type, as provided by the device itself.
     * It is up to specific types of devices to guarantee uniqueness across all devices of the same type.
     *
     * TODO: This might be useful for potential optimizations later (e.g., prevent pulling in data from the same source more than once), but for now is ignored.
     */
    abstract val deviceId: String
}


/**
 * A helper class to configure and construct immutable [DeviceRegistration] classes.
 *
 * TODO: This and extending classes are never expected to be serialized,
 *       but need to be [Serializable] since they are specified as generic type parameter on [DeviceDescriptor].
 */
@Serializable( NotSerializable::class )
abstract class DeviceRegistrationBuilder<T: DeviceRegistration>
{
    /**
     * Build the immutable [DeviceRegistration] using the current configuration of this [DeviceRegistrationBuilder].
     */
    abstract fun build(): T
}

/**
 * Should be applied to all [DeviceRegistrationBuilder] implementations to ensure misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class DeviceRegistrationBuilderDsl
