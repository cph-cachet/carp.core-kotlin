package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains any tasks which are never started by a [Trigger].
 * Tasks which are never started by a trigger are never used during the study.
 */
class UnstartedTasksWarning internal constructor() : DeploymentWarning
{
    override val description: String =
        "The study protocol contains tasks which are never started by a trigger. " +
        "Tasks which are never started by a trigger are never used during the study."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = getUnstartedTasks( protocol ).any()

    fun getUnstartedTasks( protocol: StudyProtocol ): Set<TaskConfiguration<*>>
    {
        val startedTasks: List<TaskConfiguration<*>> = protocol.triggers.flatMap { (triggerId, _) ->
            protocol.getTaskControls( triggerId )
                .filter { it.control == TaskControl.Control.Start }
                .map { it.task }
        }

        return protocol.tasks.minus( startedTasks )
    }
}
