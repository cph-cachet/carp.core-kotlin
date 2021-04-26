package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.TimeSpan
import kotlinx.serialization.Serializable


/**
 * A [TaskDescriptor] which specifies that all containing measures and/or outputs should immediately start running
 * in the background once triggered.
 * The task runs for the specified [duration], or until stopped, or until all measures and/or outputs have completed.
 *
 * TODO: Outputs are not yet specified.
 */
@Serializable
data class BackgroundTask(
    override val name: String,
    override val measures: List<Measure>,
    /**
     * The optional duration over the course of which the [measures] need to be sampled.
     * Infinite by default.
     */
    val duration: TimeSpan = TimeSpan.INFINITE
) : TaskDescriptor
