package dk.cachet.carp.common.application.triggers

import dk.cachet.carp.common.application.data.NoData
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A trigger initiated by a user, i.e., the user decides when to start a task.
 */
@Serializable
data class ManualTrigger(
    override val sourceDeviceRoleName: String,
    /**
     * A short label to describe the action performed once the user chooses to initiate this trigger.
     */
    val label: String,
    /**
     * An optional description elaborating on what happens when initiating this trigger.
     */
    val description: String? = null
) : Trigger<NoData>()
{
    @Transient
    override val requiresPrimaryDevice: Boolean = true // Software is needed to display this to the user.
}
