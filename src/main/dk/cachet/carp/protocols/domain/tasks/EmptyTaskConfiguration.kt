package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.common.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining a set of tasks ([TaskDescriptor]).
 */
class EmptyTaskConfiguration : AbstractMap<String, TaskDescriptor>(), TaskConfiguration
{
    private val _tasks: ExtractUniqueKeyMap<String, TaskDescriptor> = ExtractUniqueKeyMap(
            { task -> task.name },
            InvalidConfigurationError( "Task names within a task configuration should be unique." ) )

    override val entries: Set<Map.Entry<String, TaskDescriptor>>
        get() = _tasks.entries

    override val tasks: Set<TaskDescriptor>
        get() = _tasks.values.toSet()


    override fun addTask( task: TaskDescriptor ): Boolean
    {
        return _tasks.tryAddIfKeyIsNew( task )
    }

    override fun removeTask( task: TaskDescriptor ): Boolean
    {
        return _tasks.remove( task )
    }
}