package dk.cachet.carp.protocols.domain.triggers

import kotlinx.serialization.Serializable


/**
 * A trigger initiated by a user, i.e., the user decides when to start a task.
 */
@Serializable
data class ManualTrigger(
    /**
     * A short label to describe the action performed once the user chooses to initiate this trigger.
     */
    val label: String,
    /**
     * An optional description elaborating on what happens when initiating this trigger.
     */
    val description: String = ""
)
