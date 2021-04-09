package dk.cachet.carp.common.application.deployment

import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains any tasks which are not associated to any [Trigger].
 *
 * Tasks which are never triggered are never initiated on a device, and are thus never used during the study.
 */
class UntriggeredTasksWarning internal constructor() : DeploymentWarning
{
    override val description: String =
        "The study protocol contains tasks which are never triggered. " +
        "Tasks which are never triggered are never initiated on a device, and are thus never used during the study."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = getUntriggeredTasks( protocol ).any()

    fun getUntriggeredTasks( protocol: StudyProtocol ): Set<TaskDescriptor>
    {
        val triggeredTasks: List<TaskDescriptor> = protocol.triggers.flatMap { trigger ->
            protocol.getTriggeredTasks( trigger ).map { it.task }
        }

        return protocol.tasks.minus( triggeredTasks )
    }
}
