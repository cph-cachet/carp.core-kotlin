package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.serialization.Serializable


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
data class Smartphone( override val roleName: String ) : MasterDeviceDescriptor()