package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubDeviceDescriptor( override val roleName: String = "Stub device" ) : DeviceDescriptor<DefaultDeviceRegistrationBuilder>()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StubDeviceDescriptor::class,
                StubDeviceDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.devices.StubDeviceDescriptor" )
        }
    }

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ): Trilean = Trilean.TRUE
}