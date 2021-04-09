package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.tasks.measures.Measure
import kotlinx.serialization.Serializable


/**
 * A [TaskDescriptor] which specifies that all containing measures and/or outputs should start immediately once triggered
 * and run indefinitely until all containing measures have completed.
 */
@Serializable
data class ConcurrentTask(
    override val name: String,
    override val measures: List<Measure>
) : TaskDescriptor
