package bhrp.studyprotocols.domain.tasks

import bhrp.studyprotocols.domain.*


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
     * Add a task to this configuration.
     *
     * Throws an [InvalidConfigurationError] in case a task with the specified name already exists.
     *
     * @param task The task to add.
     * @return True if the task has been added; false if the specified [TaskDescriptor] is already included in this configuration.
     */
    fun addTask( task: TaskDescriptor ): Boolean

    /**
     * Remove a task currently present in this configuration.
     *
     * @param task The task to remove.
     * @return True if the task has been removed; false if the specified [TaskDescriptor] is not included in this configuration.
     */
    fun removeTask( task: TaskDescriptor ): Boolean
}