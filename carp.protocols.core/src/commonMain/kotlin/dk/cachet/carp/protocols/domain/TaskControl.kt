package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.TaskControl


/**
 * Specifies a task which at some point during a [StudyProtocol] needs to be started or stopped on a specific device.
 */
data class TaskControl(
    val task: TaskDescriptor,
    val targetDevice: AnyDeviceDescriptor,
    val control: TaskControl.Control
)
