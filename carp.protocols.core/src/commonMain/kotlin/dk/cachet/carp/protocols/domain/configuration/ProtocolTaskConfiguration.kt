package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.tasks.TaskConfiguration


/**
 * Defines a set of tasks ([TaskConfiguration]).
 *
 * Task names within a configuration should be unique.
 */
interface ProtocolTaskConfiguration
{
    /**
     * The tasks which measure data and/or present output on a device.
     */
    val tasks: Set<TaskConfiguration<*>>

    /**
     * Add a [task] to this configuration.
     *
     * @throws IllegalArgumentException in case a task with the specified name already exists.
     * @return True if the [task] has been added; false if it is already included in this configuration.
     */
    fun addTask( task: TaskConfiguration<*> ): Boolean

    /**
     * Remove a [task] currently present in this configuration.
     *
     * @return True if the [task] has been removed; false if it is not included in this configuration.
     */
    fun removeTask( task: TaskConfiguration<*> ): Boolean
}
