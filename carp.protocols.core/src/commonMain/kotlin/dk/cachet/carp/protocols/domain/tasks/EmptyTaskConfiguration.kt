package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining a set of tasks ([TaskDescriptor]).
 *
 * Task names of added [TaskDescriptor]s should be unique.
 */
@Suppress( "Immutable", "DataClass" )
class EmptyTaskConfiguration : AbstractMap<String, TaskDescriptor>(), TaskConfiguration
{
    private val _tasks: ExtractUniqueKeyMap<String, TaskDescriptor> =
        ExtractUniqueKeyMap( { task -> task.name } )
        {
            key -> IllegalArgumentException( "Task name \"$key\" is not unique within task configuration." )
        }

    override val entries: Set<Map.Entry<String, TaskDescriptor>>
        get() = _tasks.entries

    override val tasks: Set<TaskDescriptor>
        get() = _tasks.values.toSet()


    override fun addTask( task: TaskDescriptor ): Boolean = _tasks.tryAddIfKeyIsNew( task )

    override fun removeTask( task: TaskDescriptor ): Boolean = _tasks.remove( task )
}
