package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


@Serializable
data class StubDeviceDescriptor( override val roleName: String = "Stub device" ) : DeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StubDeviceDescriptor::class,
                StubDeviceDescriptor.serializer(),
                "dk.cachet.carp.deployment.domain.StubDeviceDescriptor" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ): Trilean = Trilean.TRUE
}