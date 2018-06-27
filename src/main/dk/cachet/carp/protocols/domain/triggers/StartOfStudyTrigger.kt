package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * A trigger which starts a task immediately at the start of a study and runs indefinitely.
 * This trigger needs to be evaluated on a master device since it is time bound and therefore requires a task scheduler.
*/
@Serializable
data class StartOfStudyTrigger( override val sourceDeviceRoleName: String ) : Trigger()
{
    @Transient
    override val requiresMasterDevice: Boolean = true

    constructor( sourceDevice: MasterDeviceDescriptor ) : this( sourceDevice.roleName )
}