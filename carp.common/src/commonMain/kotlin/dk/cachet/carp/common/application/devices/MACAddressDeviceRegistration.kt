@file:JsExport

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.MACAddress
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A [DeviceRegistration] for devices which have a MAC address.
 */
@Serializable
data class MACAddressDeviceRegistration( val macAddress: MACAddress ) : DeviceRegistration()
{
    // TODO: Remove this workaround once JS serialization bug is fixed: https://github.com/Kotlin/kotlinx.serialization/issues/716
    @Suppress( "UNNECESSARY_SAFE_CALL" )
    @Required
    override val deviceId: String = macAddress?.address
}


@Serializable( with = NotSerializable::class )
class MACAddressDeviceRegistrationBuilder : DeviceRegistrationBuilder<MACAddressDeviceRegistration>
{
    var macAddress: String = ""

    override fun build(): MACAddressDeviceRegistration = MACAddressDeviceRegistration( MACAddress.parse( macAddress ) )
}
