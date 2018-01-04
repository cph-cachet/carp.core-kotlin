package bhrp.studyprotocol.domain

import bhrp.studyprotocol.domain.deployment.*
import bhrp.studyprotocol.domain.devices.*
import bhrp.studyprotocol.domain.tasks.*
import bhrp.studyprotocol.domain.triggers.*


/**
 * A description of how a study is to be executed, defining the type(s) of master device(s) ([MasterDeviceDescriptor]) responsible for aggregating data,
 * the optional devices ([DeviceDescriptor]) connected to them, and the [Trigger]'s which lead to data collection on said devices.
 */
class StudyProtocol(
    /**
     * The person or group that created this [StudyProtocol].
     */
    val owner: ProtocolOwner,
    /**
     * A unique descriptive name for the protocol assigned by the [ProtocolOwner].
     */
    val name: String ) : StudyProtocolComposition( EmptyDeviceConfiguration(), EmptyTaskConfiguration() )
{
    /**
     * Remove a task currently present in the study protocol, including removing it from any [Trigger]'s which initiate it.
     *
     * @param task The task to remove.
     * @return True if the task has been removed; false if the specified [TaskDescriptor] is not included in this protocol.
     */
    override fun removeTask( task: TaskDescriptor ): Boolean
    {
        val isRemoved = _taskConfiguration.removeTask( task )

        // TODO: Remove task from triggers.

        return isRemoved
    }


    /**
     * Add a trigger to this protocol.
     */
    fun addTrigger( trigger: Trigger )
    {
        // TODO: Implement.
    }

    /**
     * Add a task to be sent to a device once a trigger within this protocol is initiated.
     *
     * @param trigger The trigger within this protocol which, once initiated, sends the [task] to the [device].
     * @param task The task to send to the [device].
     * @param device The device the [task] will be sent to once the [trigger] is initiated.
     */
    fun addTriggeredTask( trigger: Trigger, task: TaskDescriptor, device: DeviceDescriptor )
    {
        // TODO: Implement.
    }


    /**
     * All possible issues related to incomplete or problematic configuration of a [StudyProtocol] which might prevent deployment.
     */
    private val possibleDeploymentIssues: List<DeploymentIssue> = listOf(
        NoMasterDeviceError(),
        UntriggeredTasksWarning()
    )

    /**
     * Returns warnings and errors for the current configuration of the study protocol.
     */
    fun getDeploymentIssues(): Iterable<DeploymentIssue>
    {
        return possibleDeploymentIssues.filter { it.isIssuePresent( this ) }
    }

    /**
     * Based on the current configuration, determines whether the study protocol can be deployed.
     * The protocol can be deployed as long as no [DeploymentError]s are identified in [getDeploymentIssues].
     *
     * In order to retrieve specific deployment issues, call [getDeploymentIssues].
     */
    fun isDeployable(): Boolean
    {
        return !getDeploymentIssues().any { it is DeploymentError }
    }
}