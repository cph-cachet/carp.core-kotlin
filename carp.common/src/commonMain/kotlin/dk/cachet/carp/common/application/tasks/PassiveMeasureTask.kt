package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.tasks.measures.Measure
import kotlinx.serialization.Serializable


/**
 * A [TaskDescriptor] which solely contains [measures], all of which should start immediately once triggered.
 */
@Serializable
data class PassiveMeasureTask(
    override val name: String,
    override val measures: List<Measure>
) : TaskDescriptor
