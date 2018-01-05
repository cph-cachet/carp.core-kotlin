package bhrp.studyprotocol.domain.triggers

import bhrp.studyprotocol.domain.*
import bhrp.studyprotocol.domain.devices.DeviceDescriptor
import bhrp.studyprotocol.domain.tasks.TaskDescriptor


/**
 * Specifies a task which at some point during a [StudyProtocol] gets sent to a specific device.
 */
data class TriggeredTask( val task: TaskDescriptor, val device: DeviceDescriptor )