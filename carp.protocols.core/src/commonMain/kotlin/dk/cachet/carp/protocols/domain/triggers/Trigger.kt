package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.*


/**
 * Any condition on a device ([DeviceDescriptor]) which starts or stops tasks at certain points in time when the condition applies.
 * The condition can either be time-bound, based on data streams, initiated by a user of the platform, or a combination of these.
 */
@Serializable
abstract class Trigger : Immutable( notImmutableErrorFor( Trigger::class ) )
{
    /**
     * Determines whether the trigger needs to be evaluated on a master device ([MasterDeviceDescriptor]).
     * For example, this is the case when the trigger is time bound and needs to be evaluated by a task scheduler running on a master device.
     */
    @Transient
    open val requiresMasterDevice: Boolean = false

    /**
     * The device role name from which the trigger originates.
     */
    abstract val sourceDeviceRoleName: String
}