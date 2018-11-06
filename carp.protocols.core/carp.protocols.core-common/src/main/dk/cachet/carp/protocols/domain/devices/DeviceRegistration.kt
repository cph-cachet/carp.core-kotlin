package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON


/**
 * A [DeviceRegistration] configures a [DeviceDescriptor] as part of the deployment of a [StudyProtocol].
 */
@Serializable
open class DeviceRegistration(
    /**
     * An ID for the device, used to disambiguate between devices of the same type, as provided by the device itself.
     * It is up to specific types of devices to guarantee uniqueness across all devices of the same type.
     */
    var deviceId: String )
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( DeviceRegistration::class, "dk.cachet.carp.protocols.domain.devices.DeviceRegistration" ) }
    }

    /**
     * Make an exact copy of this object.
     */
    fun copy(): DeviceRegistration
    {
        // Use JSON serialization to make a clone.
        // This prevents each extending class from having to implement this method.
        val serialized = JSON.stringify( PolymorphicSerializer, this )
        return JSON.parse( PolymorphicSerializer, serialized ) as DeviceRegistration
    }

    override fun equals( other: Any? ): Boolean
    {
        if ( other !is DeviceRegistration )
        {
            return false
        }

        // Use JSON serialization to verify equality.
        // This prevents each extending class from having to implement this method.
        val serialized = JSON.stringify( PolymorphicSerializer, this )
        val otherSerialized = JSON.stringify( PolymorphicSerializer, this )

        return serialized == otherSerialized
    }

    override fun hashCode(): Int
    {
        // Use JSON serialization to determine hashcode.
        // This prevents each extending class from having to implement this method.
        val serialized = JSON.stringify( PolymorphicSerializer, this )
        return serialized.hashCode()
    }
}

/**
 * Create a default device registration, which solely involves assigning a unique ID to the device.
 */
fun defaultDeviceRegistration(): DeviceRegistration
{
    return DeviceRegistration( UUID.randomUUID().toString() )
}