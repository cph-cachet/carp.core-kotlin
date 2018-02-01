package carp.protocols.domain.tasks

import carp.protocols.domain.common.Immutable
import carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.*


/**
 * Describes requested measures and/or output to be presented on a device over the course of one or more specified time intervals.
 * TODO: Outputs are not yet specified.
 * TODO: Time intervals for output/measures are not yet specified.
 */
@Serializable
abstract class TaskDescriptor : Immutable( notImmutableErrorFor( TaskDescriptor::class ) )
{
    /**
     * A name which uniquely identifies the task.
     */
    @Transient
    abstract val name: String

    /**
     * The data which needs to be collected/measured as part of this task.
     */
    @Transient
    abstract val measures: Iterable<Measure>
}