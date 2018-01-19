package bhrp.studyprotocol.domain

import bhrp.studyprotocol.domain.deployment.*
import bhrp.studyprotocol.domain.devices.*
import bhrp.studyprotocol.domain.tasks.*
import bhrp.studyprotocol.domain.triggers.*


/**
 * A description of how a study is to be executed, defining the type(s) of master device(s) ([MasterDeviceDescriptor]) responsible for aggregating data,
 * the optional devices ([DeviceDescriptor]) connected to them, and the [Trigger]'s which lead to data collection on said devices.
 */
data class StudyProtocol(
    /**
     * The person or group that created this [StudyProtocol].
     */
    val owner: ProtocolOwner,
    /**
     * A unique descriptive name for the protocol assigned by the [ProtocolOwner].
     */
    val name: String ) : StudyProtocolComposition( EmptyDeviceConfiguration(), EmptyTaskConfiguration() )
{
    private val _triggers: MutableSet<Trigger> = mutableSetOf()

    /**
     * The set of triggers which can trigger tasks in this study protocol.
     */
    val triggers: Set<Trigger>
        get() = _triggers

    private val _triggeredTasks: MutableMap<Trigger, MutableSet<TriggeredTask>> = mutableMapOf()

    /**
     * Add a trigger to this protocol.
     *
     * @param trigger The trigger to add to this study protocol.
     * @return True if the [Trigger] has been added; false if the specified [Trigger] is already included in this study protocol.
     */
    fun addTrigger( trigger: Trigger ): Boolean
    {
        if ( !_deviceConfiguration.devices.contains( trigger.sourceDevice ) )
        {
            throw InvalidConfigurationError( "The passed trigger does not belong to any device specified in this study protocol." )
        }

        val isAdded: Boolean = _triggers.add( trigger )
        if ( isAdded )
        {
            _triggeredTasks.put( trigger, mutableSetOf() )
        }
        return isAdded
    }

    /**
     * Add a task to be sent to a device once a trigger within this protocol is initiated.
     * In case the trigger or task is not yet included in this study protocol, it will be added.
     * The [targetDevice] needs to be added prior to this call since it needs to be set up as either a master device or connected device.
     *
     * @param trigger The trigger which, once initiated, sends the [task] to the [targetDevice]. Either a new trigger, or one already included in the study protocol.
     * @param task The task to send to the [targetDevice]. Either a new task, or one already included in the study protocol.
     * @param targetDevice The device the [task] will be sent to once the [trigger] is initiated. The device needs to be part of the study protocol.
     * @return True if the task to be triggered has been added; false if the specified task is already triggered by the specified trigger to the specified device.
     */
    fun addTriggeredTask( trigger: Trigger, task: TaskDescriptor, targetDevice: DeviceDescriptor ): Boolean
    {
        // The device needs to be included in the study protocol. We can not add it here since we do not know whether it should be a master or connected device.
        if ( !devices.contains( targetDevice ) )
        {
            throw InvalidConfigurationError( "The passed device to which the task needs to be sent is not included in this study protocol." )
        }

        // Add trigger and task to ensure they are included in the protocol.
        addTrigger( trigger )
        _taskConfiguration.addTask( task )

        return _triggeredTasks[ trigger ]!!.add( TriggeredTask( task, targetDevice ) )
    }

    /**
     * Gets all the tasks (and the devices they are triggered to) for the specified [Trigger].
     *
     * @param trigger The [Trigger] to get the [TriggeredTask]'s for.
     */
    fun getTriggeredTasks( trigger: Trigger ): Iterable<TriggeredTask>
    {
        if ( !triggers.contains( trigger ) )
        {
            throw InvalidConfigurationError( "The passed trigger is not part of this study protocol." )
        }

        return _triggeredTasks[ trigger ]!!
    }

    /**
     * Remove a task currently present in the study protocol, including removing it from any [Trigger]'s which initiate it.
     *
     * @param task The task to remove.
     * @return True if the task has been removed; false if the specified [TaskDescriptor] is not included in this protocol.
     */
    override fun removeTask( task: TaskDescriptor ): Boolean
    {
        val isRemoved = _taskConfiguration.removeTask( task )

        // Also remove task from triggers.
        if ( isRemoved )
        {
            _triggeredTasks.map { it.value }.forEach {
                val iterator: MutableIterator<TriggeredTask> = it.iterator()
                while ( iterator.hasNext() )
                {
                    val triggeredTask = iterator.next()
                    if ( triggeredTask.task == task )
                    {
                        iterator.remove()
                    }
                }
            }
        }

        return isRemoved
    }


    /**
     * All possible issues related to incomplete or problematic configuration of a [StudyProtocol] which might prevent deployment.
     */
    private val possibleDeploymentIssues: List<DeploymentIssue> = listOf(
        NoMasterDeviceError(),
        UntriggeredTasksWarning(),
        UseCompositeTaskWarning(),
        UnusedDevicesWarning()
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