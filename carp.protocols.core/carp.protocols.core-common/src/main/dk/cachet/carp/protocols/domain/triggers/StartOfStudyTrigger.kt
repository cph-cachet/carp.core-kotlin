package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.*


/**
 * A trigger which starts a task immediately at the start of a study and runs indefinitely.
 * This trigger needs to be evaluated on a master device since it is time bound and therefore requires a task scheduler.
 */
@Serializable
data class StartOfStudyTrigger( override val sourceDeviceRoleName: String ) : Trigger()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StartOfStudyTrigger::class, "dk.cachet.carp.protocols.domain.triggers.StartOfStudyTrigger" ) }
    }

    @Transient
    override val requiresMasterDevice: Boolean = true

    constructor( sourceDevice: MasterDeviceDescriptor ) : this( sourceDevice.roleName )
}