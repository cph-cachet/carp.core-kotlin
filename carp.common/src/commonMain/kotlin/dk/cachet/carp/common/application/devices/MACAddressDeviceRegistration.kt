package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.MACAddress
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * A [DeviceRegistration] for devices which have a MAC address.
 */
@Serializable
data class MACAddressDeviceRegistration(
    val macAddress: MACAddress,
    @Required
    override val deviceDisplayName: String? = null
) : DeviceRegistration()
{
    @Required
    override val deviceId: String = macAddress.address
}


@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( with = NotSerializable::class )
class MACAddressDeviceRegistrationBuilder : DeviceRegistrationBuilder<MACAddressDeviceRegistration>()
{
    var macAddress: String = ""

    override fun build(): MACAddressDeviceRegistration =
        MACAddressDeviceRegistration( MACAddress.parse( macAddress ), deviceDisplayName )
}
