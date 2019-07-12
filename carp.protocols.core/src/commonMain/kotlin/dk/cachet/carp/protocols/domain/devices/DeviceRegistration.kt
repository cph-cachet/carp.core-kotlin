package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.*


/**
 * Custom serializer for a [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
object DeviceRegistrationSerializer : KSerializer<DeviceRegistration>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomDeviceRegistration( className, json, serializer ) } )


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
@Serializable
@Polymorphic
abstract class DeviceRegistrationBuilder
{
    /**
     * Build the immutable [DeviceRegistration] using the current configuration of this [DeviceRegistrationBuilder].
     */
    abstract fun build(): DeviceRegistration
}