package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.NotSerializable
import kotlinx.serialization.Serializable


/**
 * A [DeviceRegistration] for devices which have a MAC address.
 */
@Serializable
data class MACAddressDeviceRegistration( val macAddress: String ) : DeviceRegistration()
{
    override val deviceId: String = macAddress.toString()
}


@Serializable( with = NotSerializable::class )
class MACAddressDeviceRegistrationBuilder : DeviceRegistrationBuilder<MACAddressDeviceRegistration>
{
    var macAddress: String = ""

    override fun build(): MACAddressDeviceRegistration = MACAddressDeviceRegistration( macAddress )
}
