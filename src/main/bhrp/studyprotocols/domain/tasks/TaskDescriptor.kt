package bhrp.studyprotocols.domain.tasks

import bhrp.studyprotocols.domain.InvalidConfigurationError
import bhrp.studyprotocols.domain.common.Immutable


/**
 * Describes requested measures and/or output to be presented on a device over the course of one or more specified time intervals.
 */
abstract class TaskDescriptor
    : Immutable( InvalidConfigurationError( "Implementations of TaskDescriptor should be data classes and may not contain any mutable properties." ) )
{
    /**
     * A name which uniquely identifies the task.
     */
    abstract val name: String
}