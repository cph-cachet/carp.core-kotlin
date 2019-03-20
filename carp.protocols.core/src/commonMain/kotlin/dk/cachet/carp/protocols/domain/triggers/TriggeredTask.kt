package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor


/**
 * Specifies a task which at some point during a [StudyProtocol] gets sent to a specific device.
 */
data class TriggeredTask( val task: TaskDescriptor, val device: DeviceDescriptor )