package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.serialization.Serializable


@Serializable
data class StubMasterDeviceDescriptor( override val roleName: String = "Stub master device" ) : MasterDeviceDescriptor()