package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.Trigger


/**
 * Specifies a task which at some point during a [StudyProtocol] needs to be started or stopped on a specific device.
 */
data class TaskControl(
    val trigger: Trigger,
    val task: TaskDescriptor,
    val destinationDevice: AnyDeviceDescriptor,
    val control: TaskControl.Control
)


/**
 * Specify that a [task] should start on the specified [destinationDevice] once this [Trigger] initiates.
 */
fun Trigger.start( task: TaskDescriptor, destinationDevice: AnyDeviceDescriptor ) =
    TaskControl( this, task, destinationDevice, TaskControl.Control.Start )

/**
 * Specify that a [task] should stop on the specified [destinationDevice] once this [Trigger] initiates.
 */
fun Trigger.stop( task: TaskDescriptor, destinationDevice: AnyDeviceDescriptor ) =
    TaskControl( this, task, destinationDevice, TaskControl.Control.Stop )
