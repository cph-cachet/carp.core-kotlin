package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.Serializable


/**
 * Custom serializer for a [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
object DeviceRegistrationSerializer : UnknownPolymorphicSerializer<DeviceRegistration, CustomDeviceRegistration>( CustomDeviceRegistration::class )
{
    override fun createWrapper( className: String, json: String ): CustomDeviceRegistration = CustomDeviceRegistration( className, json )
}


/**
 * A [DeviceRegistration] configures a [DeviceDescriptor] as part of the deployment of a [StudyProtocol].
 */
@Serializable
abstract class DeviceRegistration
{
    /**
     * An ID for the device, used to disambiguate between devices of the same type, as provided by the device itself.
     * It is up to specific types of devices to guarantee uniqueness across all devices of the same type.
     *
     * TODO: This might be useful for potential optimizations later (e.g., prevent pulling in data from the same source more than once), but for now is ignored.
     */
    abstract var deviceId: String

    /**
     * Make an exact copy of this object.
     */
    fun copy(): DeviceRegistration
    {
        // Use JSON serialization to make a clone.
        // This prevents each extending class from having to implement this method.
        val serialized = JSON.stringify( DeviceRegistrationSerializer, this )
        return JSON.parse( DeviceRegistrationSerializer, serialized )
    }

    override fun equals( other: Any? ): Boolean
    {
        if ( other !is DeviceRegistration )
        {
            return false
        }

        // Use JSON serialization to verify equality.
        // This prevents each extending class from having to implement this method.
        val serialized = JSON.stringify( DeviceRegistrationSerializer, this )
        val otherSerialized = JSON.stringify( DeviceRegistrationSerializer, this )

        return serialized == otherSerialized
    }

    override fun hashCode(): Int
    {
        // Use JSON serialization to determine hashcode.
        // This prevents each extending class from having to implement this method.
        val serialized = JSON.stringify( DeviceRegistrationSerializer, this )
        return serialized.hashCode()
    }
}


/**
 * Create a default device registration, which solely involves assigning a unique ID to the device.
 */
fun defaultDeviceRegistration(): DeviceRegistration
{
    return DefaultDeviceRegistration( UUID.randomUUID().toString() )
}