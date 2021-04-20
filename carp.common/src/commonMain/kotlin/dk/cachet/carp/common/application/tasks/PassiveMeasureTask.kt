package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.tasks.measures.PassiveMeasure
import kotlinx.serialization.Serializable


/**
 * A [TaskDescriptor] which solely contains [PassiveMeasure]'s, all of which should start immediately once triggered.
 */
@Serializable
data class PassiveMeasureTask(
    override val name: String,
    override val measures: List<PassiveMeasure>
) : TaskDescriptor
