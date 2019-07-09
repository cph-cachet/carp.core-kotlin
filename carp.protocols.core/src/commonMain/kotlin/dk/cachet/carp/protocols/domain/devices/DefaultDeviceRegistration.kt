package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * A concrete [DeviceRegistration] which solely implements the base properties and nothing else.
 *
 * The base class can't be made concrete since this would prevent being able to serialize extending types (constructors may not contain parameters which are not properties).
 */
@Serializable
data class DefaultDeviceRegistration( override val deviceId: String ) : DeviceRegistration()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                DefaultDeviceRegistration::class,
                DefaultDeviceRegistration.serializer(),
                "dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration" )
        }
    }
}


/**
 * A default device registration builder which solely involves assigning a unique ID to the device.
 * By default, a unique ID (UUID) is generated.
 */
@Serializable
class DefaultDeviceRegistrationBuilder( private var deviceId: String = UUID.randomUUID().toString() ) : DeviceRegistrationBuilder()
{
    /**
     * Override the default assigned UUID which has been set as device ID.
     * Make sure this ID is unique for the type of device you are creating a registration for.
     */
    fun deviceId( getId: () -> String ) { this.deviceId = getId() }

    override fun build(): DefaultDeviceRegistration = DefaultDeviceRegistration( deviceId )
}