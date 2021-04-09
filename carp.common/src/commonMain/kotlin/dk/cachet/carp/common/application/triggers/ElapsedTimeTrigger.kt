package dk.cachet.carp.common.application.triggers

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A trigger which starts a task after a specified amount of time has elapsed since the start of a study deployment.
 * The start of a study deployment is determined by the first successful deployment of all participating devices.
 * This trigger needs to be evaluated on a master device since it is time bound and therefore requires a task scheduler.
 */
@Suppress( "DataClassPrivateConstructor" )
@Serializable
data class ElapsedTimeTrigger private constructor(
    override val sourceDeviceRoleName: String,
    val elapsedTime: TimeSpan
) : Trigger()
{
    @Transient
    override val requiresMasterDevice: Boolean = true

    constructor( sourceDevice: AnyMasterDeviceDescriptor, elapsedTime: TimeSpan ) :
        this( sourceDevice.roleName, elapsedTime )
}
