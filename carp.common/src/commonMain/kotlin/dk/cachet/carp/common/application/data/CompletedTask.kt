package dk.cachet.carp.common.application.data

import kotlinx.serialization.*


/**
 * Indicates the task with [taskName] was completed.
 * [taskData] holds the result of a completed interactive task, or null if no result is expected.
 */
@Serializable
@SerialName( CarpDataTypes.COMPLETED_TASK_TYPE_NAME )
data class CompletedTask( val taskName: String, val taskData: Data? = null ) : Data
