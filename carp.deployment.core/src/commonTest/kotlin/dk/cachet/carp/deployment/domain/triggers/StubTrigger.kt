package dk.cachet.carp.deployment.domain.triggers

import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.triggers.Trigger
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
