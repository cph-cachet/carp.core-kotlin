package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


@Serializable
data class StubDeviceDescriptor( override val roleName: String = "Stub device" ) : DeviceDescriptor<DefaultDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ): Trilean = Trilean.TRUE
}