package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.tasks.BackgroundTask
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains any [BackgroundTask] without any measures.
 * Since a [BackgroundTask] doesn't do anything else than collect passive measures,
 * a [BackgroundTask] without any measures is a non-operation.
 */
class BackgroundTaskWithNoMeasuresWarning internal constructor() : DeploymentWarning
{
    override val description: String =
        "The study protocol contains a background task which doesn't contain any measures. " +
        "A background task without any measures doesn't do anything."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = getBackgroundTasksWithNoMeasures( protocol ).any()

    fun getBackgroundTasksWithNoMeasures( protocol: StudyProtocol ): Set<BackgroundTask> = protocol
        .tasks
        .filterIsInstance<BackgroundTask>()
        .filter { it.measures.isEmpty() }
        .toSet()
}
