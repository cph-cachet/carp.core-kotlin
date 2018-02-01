package carp.protocols.domain.devices

import kotlinx.serialization.Serializable


@Serializable
data class StubMasterDeviceDescriptor( override val roleName: String = "Stub master device" ) : MasterDeviceDescriptor()