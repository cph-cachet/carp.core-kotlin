package bhrp.studyprotocols.domain.tasks

import bhrp.studyprotocols.domain.common.Immutable
import bhrp.studyprotocols.domain.notImmutableErrorFor


/**
 * Describes requested measures and/or output to be presented on a device over the course of one or more specified time intervals.
 * TODO: Outputs are not yet specified.
 * TODO: Time intervals for output/measures are not yet specified.
 */
abstract class TaskDescriptor : Immutable( notImmutableErrorFor( TaskDescriptor::class ) )
{
    /**
     * A name which uniquely identifies the task.
     */
    abstract val name: String

    /**
     * The data which needs to be collected/measured as part of this task.
     */
    abstract val measures: Iterable<Measure>
}