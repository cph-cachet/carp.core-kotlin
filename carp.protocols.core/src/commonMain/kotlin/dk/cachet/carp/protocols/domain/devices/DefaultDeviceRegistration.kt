package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.NotSerializable
import kotlinx.serialization.Serializable


/**
 * A concrete [DeviceRegistration] which solely implements the base properties and nothing else.
 *
 * The base class can't be made concrete since this would prevent being able to serialize extending types (constructors may not contain parameters which are not properties).
 */
@Serializable
data class DefaultDeviceRegistration( override val deviceId: String ) : DeviceRegistration()


/**
 * A default device registration builder which solely involves assigning a unique ID to the device.
 * By default, a unique ID (UUID) is generated.
 *
 * @param deviceId
 *   Override the default assigned UUID which has been set as device ID.
 *   Make sure this ID is unique for the type of device you are creating a registration for.
 */
@Serializable( with = NotSerializable::class )
@DeviceRegistrationBuilderDsl
class DefaultDeviceRegistrationBuilder( var deviceId: String = UUID.randomUUID().toString() )
    : DeviceRegistrationBuilder<DefaultDeviceRegistration>()
{
    override fun build(): DefaultDeviceRegistration = DefaultDeviceRegistration( deviceId )
}
