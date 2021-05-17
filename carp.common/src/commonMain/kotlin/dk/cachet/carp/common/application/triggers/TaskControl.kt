package dk.cachet.carp.common.application.triggers

import kotlinx.serialization.Serializable


/**
 * Specifies that once a condition of the trigger with [triggerId] applies,
 * the task with [taskName] on [destinationDeviceRoleName] should be started or stopped.
 */
@Serializable
data class TaskControl(
    val triggerId: Int,
    val taskName: String,
    val destinationDeviceRoleName: String,
    val control: Control
)
{
    /**
     * Determines what to do with a task once the condition of a trigger is met.
     */
    enum class Control { Start, Stop }
}
