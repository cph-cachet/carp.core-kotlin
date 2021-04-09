package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.tasks.measures.Measure
import kotlinx.serialization.Polymorphic


/**
 * Describes requested measures and/or output to be presented on a device.
 * TODO: Outputs are not yet specified.
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface TaskDescriptor
{
    /**
     * A name which uniquely identifies the task.
     */
    val name: String

    /**
     * The data which needs to be collected/measured as part of this task.
     */
    val measures: List<Measure>
}
