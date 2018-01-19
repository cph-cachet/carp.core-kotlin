package bhrp.studyprotocols.domain.triggers

import bhrp.studyprotocols.domain.*
import bhrp.studyprotocols.domain.devices.DeviceDescriptor
import bhrp.studyprotocols.domain.tasks.TaskDescriptor


/**
 * Specifies a task which at some point during a [StudyProtocol] gets sent to a specific device.
 */
data class TriggeredTask( val task: TaskDescriptor, val device: DeviceDescriptor )