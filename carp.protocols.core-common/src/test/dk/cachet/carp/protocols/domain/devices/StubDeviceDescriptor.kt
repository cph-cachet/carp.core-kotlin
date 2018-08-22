package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.serialization.Serializable


@Serializable
data class StubDeviceDescriptor( override val roleName: String = "Stub device" ) : DeviceDescriptor()