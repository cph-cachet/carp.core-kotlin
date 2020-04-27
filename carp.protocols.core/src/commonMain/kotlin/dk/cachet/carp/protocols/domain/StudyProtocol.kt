package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.deployment.DeploymentError
import dk.cachet.carp.protocols.domain.deployment.DeploymentIssue
import dk.cachet.carp.protocols.domain.deployment.NoMasterDeviceError
import dk.cachet.carp.protocols.domain.deployment.UntriggeredTasksWarning
import dk.cachet.carp.protocols.domain.deployment.UnusedDevicesWarning
import dk.cachet.carp.protocols.domain.deployment.UseCompositeTaskWarning
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.EmptyDeviceConfiguration
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.EmptyTaskConfiguration
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.protocols.domain.triggers.TriggeredTask


/**
 * A description of how a study is to be executed, defining the type(s) of master device(s) ([AnyMasterDeviceDescriptor]) responsible for aggregating data,
 * the optional devices ([AnyDeviceDescriptor]) connected to them, and the [Trigger]'s which lead to data collection on said devices.
 */
@Suppress( "TooManyFunctions" ) // TODO: some of the device and task configuration methods are overridden solely to add events. Can this be refactored?
class StudyProtocol(
    /**
     * The person or group that created this [StudyProtocol].
     */
    val owner: ProtocolOwner,
    /**
     * A unique descriptive name for the protocol assigned by the [ProtocolOwner].
     */
    val name: String,
    /**
     * An optional description for the study protocol.
     */
    val description: String = ""
) : StudyProtocolComposition( EmptyDeviceConfiguration(), EmptyTaskConfiguration() )
{
    sealed class Event : Immutable()
    {
        data class MasterDeviceAdded( val device: AnyMasterDeviceDescriptor ) : Event()
        data class ConnectedDeviceAdded( val connected: AnyDeviceDescriptor, val master: AnyMasterDeviceDescriptor ) : Event()
        data class TriggerAdded( val trigger: Trigger ) : Event()
        data class TaskAdded( val task: TaskDescriptor ) : Event()
        data class TaskRemoved( val task: TaskDescriptor ) : Event()
        data class TriggeredTaskAdded( val triggeredTask: TriggeredTask ) : Event()
        data class TriggeredTaskRemoved( val triggeredTask: TriggeredTask ) : Event()
    }


    companion object Factory
    {
        fun fromSnapshot( snapshot: StudyProtocolSnapshot ): StudyProtocol
        {
            val owner = ProtocolOwner( snapshot.ownerId )
            val protocol = StudyProtocol( owner, snapshot.name )
            protocol.creationDate = snapshot.creationDate

            // Add master devices.
            snapshot.masterDevices.forEach { protocol.addMasterDevice( it ) }

            // Add connected devices.
            val allDevices: List<AnyDeviceDescriptor> = snapshot.connectedDevices.plus( snapshot.masterDevices ).toList()
            snapshot.connections.forEach { c ->
                val master: AnyMasterDeviceDescriptor = allDevices.filterIsInstance<AnyMasterDeviceDescriptor>().firstOrNull { it.roleName == c.connectedToRoleName }
                    ?: throw InvalidConfigurationError( "Can't find master device with role name '${c.connectedToRoleName}' in snapshot." )
                val connected: AnyDeviceDescriptor = allDevices.firstOrNull { it.roleName == c.roleName }
                    ?: throw InvalidConfigurationError( "Can't find connected device with role name '${c.roleName}' in snapshot." )
                protocol.addConnectedDevice( connected, master )
            }

            // Add tasks and triggers.
            snapshot.tasks.forEach { protocol.addTask( it ) }
            snapshot.triggers.forEach { protocol.addTrigger( it.value ) }

            // Add triggered tasks.
            snapshot.triggeredTasks.forEach { triggeredTask ->
                val triggerMatch = snapshot.triggers.entries.singleOrNull { it.key == triggeredTask.triggerId }
                    ?: throw InvalidConfigurationError( "Can't find trigger with id '${triggeredTask.triggerId}' in snapshot." )
                val task: TaskDescriptor = protocol.tasks.singleOrNull { it.name == triggeredTask.taskName }
                    ?: throw InvalidConfigurationError( "Can't find task with name '${triggeredTask.taskName}' in snapshot." )
                val device: AnyDeviceDescriptor = protocol.devices.singleOrNull { it.roleName == triggeredTask.targetDeviceRoleName }
                    ?: throw InvalidConfigurationError( "Can't find device with role name '${triggeredTask.targetDeviceRoleName}' in snapshot." )
                protocol.addTriggeredTask( triggerMatch.value, task, device )
            }

            return protocol
        }
    }


    /**
     * The date when this protocol was created.
     */
    var creationDate: DateTime = DateTime.now()
        private set

    /**
     * Add a master device which is responsible for aggregating and synchronizing incoming data.
     *
     * Throws an [InvalidConfigurationError] in case a device with the specified role name already exists.
     *
     * @param masterDevice A description of the master device to add. Its role name should be unique in the protocol.
     * @return True if the device has been added; false if the specified [MasterDeviceDescriptor] is already set as a master device.
     */
    override fun addMasterDevice( masterDevice: AnyMasterDeviceDescriptor ): Boolean
    {
        val added = super.addMasterDevice( masterDevice )
        if ( added )
        {
            event( Event.MasterDeviceAdded( masterDevice ) )
        }
        return added
    }

    /**
     * Add a device which is connected to a master device within this configuration.
     *
     * Throws an [InvalidConfigurationError] in case a device with the specified role name already exists.
     *
     * @param device The device to be connected to a master device. Its role name should be unique in the protocol.
     * @param masterDevice The master device to connect to.
     * @return True if the device has been added; false if the specified [DeviceDescriptor] is already connected to the specified [MasterDeviceDescriptor].
     */
    override fun addConnectedDevice( device: AnyDeviceDescriptor, masterDevice: AnyMasterDeviceDescriptor ): Boolean
    {
        val added = super.addConnectedDevice(device, masterDevice)
        if ( added )
        {
            event( Event.ConnectedDeviceAdded( device, masterDevice ) )
        }
        return added
    }

    private val _triggers: MutableSet<Trigger> = mutableSetOf()

    /**
     * The set of triggers which can trigger tasks in this study protocol.
     */
    val triggers: Set<Trigger>
        get() = _triggers

    private val triggeredTasks: MutableMap<Trigger, MutableSet<TriggeredTask>> = mutableMapOf()

    /**
     * Add a trigger to this protocol.
     *
     * @param trigger The trigger to add to this study protocol.
     * @return True if the [Trigger] has been added; false if the specified [Trigger] is already included in this study protocol.
     */
    fun addTrigger( trigger: Trigger ): Boolean
    {
        val device: AnyDeviceDescriptor = deviceConfiguration.devices.firstOrNull { it.roleName == trigger.sourceDeviceRoleName }
            ?: throw InvalidConfigurationError( "The passed trigger does not belong to any device specified in this study protocol." )

        if ( trigger.requiresMasterDevice && device !is AnyMasterDeviceDescriptor )
        {
            throw InvalidConfigurationError( "The passed trigger cannot be initiated by the specified device since it is not a master device." )
        }

        val isAdded: Boolean = _triggers.add( trigger )
        if ( isAdded )
        {
            triggeredTasks[ trigger ] = mutableSetOf()
            event( Event.TriggerAdded( trigger ) )
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
    fun addTriggeredTask( trigger: Trigger, task: TaskDescriptor, targetDevice: AnyDeviceDescriptor ): Boolean
    {
        // The device needs to be included in the study protocol. We can not add it here since we do not know whether it should be a master or connected device.
        if ( targetDevice !in devices )
        {
            throw InvalidConfigurationError( "The passed device to which the task needs to be sent is not included in this study protocol." )
        }

        // Add trigger and task to ensure they are included in the protocol.
        addTrigger( trigger )
        addTask( task )

        // Add triggered task.
        val triggeredTask = TriggeredTask( task, targetDevice )
        val triggeredTaskAdded = triggeredTasks[ trigger ]!!.add( triggeredTask )
        if ( triggeredTaskAdded )
        {
            event( Event.TriggeredTaskAdded( triggeredTask ) )
        }

        return triggeredTaskAdded
    }

    /**
     * Gets all the tasks (and the devices they are triggered to) for the specified [Trigger].
     *
     * @param trigger The [Trigger] to get the [TriggeredTask]'s for.
     */
    fun getTriggeredTasks( trigger: Trigger ): Iterable<TriggeredTask>
    {
        if ( trigger !in triggers )
        {
            throw InvalidConfigurationError( "The passed trigger is not part of this study protocol." )
        }

        return triggeredTasks[ trigger ]!!
    }

    /**
     * Gets all the tasks triggered for the specified [device].
     */
    fun getTasksForDevice( device: AnyDeviceDescriptor ): Set<TaskDescriptor>
    {
        return triggeredTasks
            .flatMap { it.value }
            .filter { it.targetDevice == device }
            .map { it.task }
            .toSet()
    }

    /**
     * Add a task to this configuration.
     *
     * Throws an [InvalidConfigurationError] in case a task with the specified name already exists.
     *
     * @param task The task to add.
     * @return True if the task has been added; false if the specified [TaskDescriptor] is already included in this configuration.
     */
    override fun addTask( task: TaskDescriptor ): Boolean
    {
        val added = super.addTask( task )
        if ( added )
        {
            event( Event.TaskAdded( task ) )
        }
        return added
    }

    /**
     * Remove a task currently present in the study protocol, including removing it from any [Trigger]'s which initiate it.
     *
     * @param task The task to remove.
     * @return True if the task has been removed; false if the specified [TaskDescriptor] is not included in this protocol.
     */
    override fun removeTask( task: TaskDescriptor ): Boolean
    {
        // Remove task from triggers.
        triggeredTasks.map { it.value }.forEach {
            val triggeredTasks = it.filter { triggered -> triggered.task == task }
            it.removeAll( triggeredTasks )
            triggeredTasks.forEach { event( Event.TriggeredTaskRemoved( it ) ) }
        }

        // Remove task itself.
        val isRemoved = taskConfiguration.removeTask( task )
        if ( isRemoved )
        {
            event( Event.TaskRemoved( task ) )
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
    fun getDeploymentIssues(): Iterable<DeploymentIssue> =
        possibleDeploymentIssues.filter { it.isIssuePresent( this ) }

    /**
     * Based on the current configuration, determines whether the study protocol can be deployed.
     * The protocol can be deployed as long as no [DeploymentError]s are identified in [getDeploymentIssues].
     *
     * In order to retrieve specific deployment issues, call [getDeploymentIssues].
     */
    fun isDeployable(): Boolean =
        !getDeploymentIssues().any { it is DeploymentError }


    /**
     * Get a serializable snapshot of the current state of this [StudyProtocol].
     */
    override fun getSnapshot(): StudyProtocolSnapshot = StudyProtocolSnapshot.fromProtocol( this )
}
