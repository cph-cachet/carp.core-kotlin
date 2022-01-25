package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * A concrete [DeviceRegistration] which solely implements the base properties and nothing else.
 *
 * The base class can't be made concrete since this would prevent being able to serialize extending types (constructors may not contain parameters which are not properties).
 */
@Serializable
data class DefaultDeviceRegistration(
    @Required
    override val deviceDisplayName: String? = null,
    @Required
    override val deviceId: String = UUID.randomUUID().toString()
) : DeviceRegistration()


/**
 * A default device registration builder which solely involves assigning a display name and unique ID to the device.
 * By default, a unique ID (UUID) is generated.
 */
@Serializable( with = NotSerializable::class )
class DefaultDeviceRegistrationBuilder : DeviceRegistrationBuilder<DefaultDeviceRegistration>()
{
    /**
     * Override the default assigned UUID which has been set as device ID.
     * Make sure this ID is unique for the type of device you are creating a registration for.
     */
    var deviceId: String = UUID.randomUUID().toString()

    override fun build(): DefaultDeviceRegistration = DefaultDeviceRegistration( deviceDisplayName, deviceId )
}
