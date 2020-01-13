package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A trigger which starts a task immediately at the start of a study and runs indefinitely.
 * This trigger needs to be evaluated on a master device since it is time bound and therefore requires a task scheduler.
 */
@Suppress( "DataClassPrivateConstructor" )
@Serializable
data class StartOfStudyTrigger private constructor( override val sourceDeviceRoleName: String ) : Trigger()
{
    @Transient
    override val requiresMasterDevice: Boolean = true

    constructor( sourceDevice: AnyMasterDeviceDescriptor ) : this( sourceDevice.roleName )
}
