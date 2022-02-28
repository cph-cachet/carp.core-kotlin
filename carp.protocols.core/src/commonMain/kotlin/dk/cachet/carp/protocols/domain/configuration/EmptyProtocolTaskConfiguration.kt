package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining a set of tasks ([TaskConfiguration]).
 *
 * Task names of added [TaskConfiguration]s should be unique.
 */
@Suppress( "Immutable", "DataClass" )
class EmptyProtocolTaskConfiguration : AbstractMap<String, TaskConfiguration<*>>(), ProtocolTaskConfiguration
{
    private val _tasks: ExtractUniqueKeyMap<String, TaskConfiguration<*>> =
        ExtractUniqueKeyMap( { task -> task.name } )
        {
            key -> IllegalArgumentException( "Task name \"$key\" is not unique within task configuration." )
        }

    override val entries: Set<Map.Entry<String, TaskConfiguration<*>>>
        get() = _tasks.entries

    override val tasks: Set<TaskConfiguration<*>>
        get() = _tasks.values.toSet()


    override fun addTask( task: TaskConfiguration<*> ): Boolean = _tasks.tryAddIfKeyIsNew( task )

    override fun removeTask( task: TaskConfiguration<*> ): Boolean = _tasks.remove( task )
}
