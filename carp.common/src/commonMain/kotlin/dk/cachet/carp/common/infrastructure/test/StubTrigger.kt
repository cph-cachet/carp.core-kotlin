package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class StubTrigger(
    override val sourceDeviceRoleName: String,
    val uniqueProperty: String = "Unique",
    @Transient
    override val requiresMasterDevice: Boolean = false
) : Trigger()
{
    constructor( device: AnyDeviceDescriptor ) : this( device, "Unique" )
    constructor( device: AnyDeviceDescriptor, uniqueName: String ) : this( device.roleName, uniqueName )
}
