package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.triggers.TaskControl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Indicates the task with [taskName] was started or stopped ([control]) by the trigger with [triggerId]
 * on the device with [destinationDeviceRoleName], referring to identifiers in the study protocol.
 * [triggerData] may contain additional information related to the circumstances which caused the trigger to fire.
 */
@Serializable
@SerialName( CarpDataTypes.TRIGGERED_TASK_TYPE_NAME )
data class TriggeredTask(
    val triggerId: Int,
    val taskName: String,
    val destinationDeviceRoleName: String,
    val control: TaskControl.Control,
    val triggerData: Data? = null
) : Data
