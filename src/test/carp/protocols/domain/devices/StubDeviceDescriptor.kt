package carp.protocols.domain.devices

import kotlinx.serialization.Serializable


@Serializable
data class StubDeviceDescriptor( override val roleName: String = "Stub device" ) : DeviceDescriptor()