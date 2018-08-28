package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.serialization.Serializable


@Serializable
data class StubTrigger(
    override val sourceDeviceRoleName: String,
    val uniqueProperty: String = "Unique" ) : Trigger()
{
    constructor( device: DeviceDescriptor ) : this( device, "Unique" )
    constructor( device: DeviceDescriptor, uniqueName: String ) : this( device.roleName, uniqueName )
}