package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import kotlinx.serialization.Serializable


@Serializable
data class StubMasterDeviceDescriptor( override val roleName: String = "Stub master device" ) : MasterDeviceDescriptor<DefaultDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}