package carp.protocols.domain.triggers

import carp.protocols.domain.*
import carp.protocols.domain.devices.DeviceDescriptor
import carp.protocols.domain.tasks.TaskDescriptor


/**
 * Specifies a task which at some point during a [StudyProtocol] gets sent to a specific device.
 */
data class TriggeredTask( val task: TaskDescriptor, val device: DeviceDescriptor )