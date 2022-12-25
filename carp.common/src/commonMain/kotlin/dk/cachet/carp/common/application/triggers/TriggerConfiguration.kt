package dk.cachet.carp.common.application.triggers

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.application.devices.PrimaryDeviceConfiguration
import kotlinx.serialization.*


/**
 * Any condition on a device ([DeviceConfiguration]) used to start or stop tasks at certain points in time when the condition applies.
 * The condition can either be time-bound, based on data streams, initiated by a user of the platform, or a combination of these.
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
abstract class TriggerConfiguration<TData : Data>
{
    /**
     * Determines whether the trigger needs to be evaluated on a primary device ([PrimaryDeviceConfiguration]).
     * For example, this is the case when the trigger is time bound and needs to be evaluated by a task scheduler running on a primary device.
     */
    @Transient
    open val requiresPrimaryDevice: Boolean = false

    /**
     * The device role name from which the trigger originates.
     */
    abstract val sourceDeviceRoleName: String
}
