package carp.protocols.domain.devices


data class StubMasterDeviceDescriptor( override val roleName: String = "Mock master device" ) : MasterDeviceDescriptor()