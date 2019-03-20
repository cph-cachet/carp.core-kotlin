package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubMasterDeviceDescriptor( override val roleName: String = "Stub master device" ) : MasterDeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StubMasterDeviceDescriptor::class,
                StubMasterDeviceDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.devices.StubMasterDeviceDescriptor" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}