package bhrp.studyprotocols.domain.tasks

import bhrp.studyprotocols.domain.common.Immutable
import bhrp.studyprotocols.domain.notImmutableErrorFor


/**
 * Describes requested measures and/or output to be presented on a device over the course of one or more specified time intervals.
 */
abstract class TaskDescriptor : Immutable( notImmutableErrorFor( TaskDescriptor::class ) )
{
    /**
     * A name which uniquely identifies the task.
     */
    abstract val name: String
}