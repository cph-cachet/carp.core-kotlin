package dk.cachet.carp.common.domain.tasks

import dk.cachet.carp.common.application.tasks.TaskDescriptor


/**
 * Defines a set of tasks ([TaskDescriptor]).
 *
 * Task names within a configuration should be unique.
 */
interface TaskConfiguration
{
    /**
     * The tasks which measure data and/or present output on a device.
     */
    val tasks: Set<TaskDescriptor>

    /**
     * Add a [task] to this configuration.
     *
     * @throws IllegalArgumentException in case a task with the specified name already exists.
     * @return True if the [task] has been added; false if it is already included in this configuration.
     */
    fun addTask( task: TaskDescriptor ): Boolean

    /**
     * Remove a [task] currently present in this configuration.
     *
     * @return True if the [task] has been removed; false if it is not included in this configuration.
     */
    fun removeTask( task: TaskDescriptor ): Boolean
}
