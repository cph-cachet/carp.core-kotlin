package dk.cachet.carp.common.application.triggers

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration


/**
 * A trigger which starts a task after a specified amount of time has elapsed since the start of a study deployment.
 * The start of a study deployment is determined by the first successful deployment of all participating devices.
 * This trigger needs to be evaluated on a primary device since it is time bound and therefore requires a task scheduler.
 */
@Suppress( "DataClassPrivateConstructor" )
@Serializable
data class ElapsedTimeTrigger private constructor(
    override val sourceDeviceRoleName: String,
    @Serializable( DurationSerializer::class )
    val elapsedTime: Duration
) : Trigger<NoData>()
{
    @Transient
    override val requiresPrimaryDevice: Boolean = true

    constructor( sourceDevice: AnyPrimaryDeviceConfiguration, elapsedTime: Duration ) :
        this( sourceDevice.roleName, elapsedTime )
}
