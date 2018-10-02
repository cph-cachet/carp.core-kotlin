package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubDeviceDescriptor( override val roleName: String = "Stub device" ) : DeviceDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubDeviceDescriptor::class, "dk.cachet.carp.protocols.domain.devices.StubDeviceDescriptor" ) }
    }
}