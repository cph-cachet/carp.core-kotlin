package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.*


/**
 * Describes requested measures and/or output to be presented on a device.
 * TODO: Outputs are not yet specified.
 */
@Serializable
@Polymorphic
abstract class TaskDescriptor : Immutable( notImmutableErrorFor( TaskDescriptor::class ) )
{
    /**
     * A name which uniquely identifies the task.
     */
    abstract val name: String

    /**
     * The data which needs to be collected/measured as part of this task.
     */
    abstract val measures: List<Measure>
}