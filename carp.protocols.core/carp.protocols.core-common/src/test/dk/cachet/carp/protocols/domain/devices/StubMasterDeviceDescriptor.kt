package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubMasterDeviceDescriptor( override val roleName: String = "Stub master device" ) : MasterDeviceDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubMasterDeviceDescriptor::class, "dk.cachet.carp.protocols.domain.devices.StubMasterDeviceDescriptor" ) }
    }
}