package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.infrastructure.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration


/**
 * A [TaskConfiguration] which specifies that all containing measures and/or outputs should immediately start running
 * in the background once triggered.
 * The task runs for the specified [duration], or until stopped, or until all measures and/or outputs have completed.
 *
 * TODO: Outputs are not yet specified.
 */
@Serializable
data class BackgroundTask(
    override val name: String,
    override val measures: List<Measure> = emptyList(),
    override val description: String? = null,
    /**
     * The optional duration over the course of which the [measures] need to be sampled.
     * Infinite by default.
     */
    @Serializable( DurationSerializer::class )
    val duration: Duration = Duration.INFINITE
) : TaskConfiguration<NoData> // Not an interactive task, so uploads no data other than measures.


/**
 * A helper class to configure and construct immutable [BackgroundTask] instances.
 */
class BackgroundTaskBuilder(
    /**
     * The optional duration over the course of which the [measures] need to be sampled.
     * Infinite by default.
     */
    var duration: Duration = Duration.INFINITE
) : TaskConfigurationBuilder<BackgroundTask>()
{
    override fun build( name: String ): BackgroundTask =
        BackgroundTask( name, measures, description, duration )
}
