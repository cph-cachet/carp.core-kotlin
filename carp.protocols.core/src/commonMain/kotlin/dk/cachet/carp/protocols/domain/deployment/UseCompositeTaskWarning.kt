package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.triggers.Trigger


/**
 * Evaluates whether a [StudyProtocol] contains any triggers which send multiple tasks to a single target device.
 *
 * Such 'overlapping' tasks are recommended to be modeled as a single composite task instead, for clarity and to circumvent potential concurrency issues.
 */
class UseCompositeTaskWarning internal constructor() : DeploymentWarning
{
    /**
     * Holds [tasks] which are send by a single [trigger] to a single [targetDevice] when initiated.
     * When the [trigger] is initiated, the tasks would thus be sent out simultaneously to the [targetDevice].
     */
    data class OverlappingTasks( val trigger: Trigger, val targetDevice: AnyDeviceDescriptor, val tasks: List<TaskDescriptor> )

    override val description: String =
        "The study protocol contains triggers which send multiple tasks to a single device. " +
        "It is recommended to model this as one composite task instead, for clarity and to circumvent potential concurrency issues."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean
    {
        return getOverlappingTasks( protocol ).any()
    }

    fun getOverlappingTasks( protocol: StudyProtocol ): List<OverlappingTasks>
    {
        return protocol.triggers.flatMap { trigger -> // For each trigger, ...
            protocol
                .getTriggeredTasks( trigger )
                .groupBy { it.targetDevice } // ... group triggered tasks by target device, ...
                .filter { it.value.count() > 1 } // ... select those with more than one task triggered (to a single target device).
                .map { taskPerDevice ->
                    // Transform to data class which holds the trigger, device, and overlapping tasks
                    val overlappingTasks = taskPerDevice.value.map { it.task }
                    OverlappingTasks( trigger, taskPerDevice.key, overlappingTasks )
                }
        }
    }
}