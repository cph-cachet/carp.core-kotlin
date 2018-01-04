package bhrp.studyprotocol.domain.devices


data class StubMasterDeviceDescriptor( override val roleName: String = "Mock master device" ) : MasterDeviceDescriptor()