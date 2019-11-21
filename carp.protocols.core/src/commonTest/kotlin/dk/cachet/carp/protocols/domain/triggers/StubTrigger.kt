package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import kotlinx.serialization.*


@Serializable
data class StubTrigger(
    override val sourceDeviceRoleName: String,
    val uniqueProperty: String = "Unique",
    @Transient
    override val requiresMasterDevice: Boolean = false ) : Trigger()
{
    constructor( device: AnyDeviceDescriptor ) : this( device, "Unique" )
    constructor( device: AnyDeviceDescriptor, uniqueName: String ) : this( device.roleName, uniqueName )
}


