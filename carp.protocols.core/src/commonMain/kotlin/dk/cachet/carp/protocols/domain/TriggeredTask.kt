package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor


/**
 * Specifies a task which at some point during a [StudyProtocol] gets sent to a specific device.
 */
data class TriggeredTask( val task: TaskDescriptor, val targetDevice: AnyDeviceDescriptor )
