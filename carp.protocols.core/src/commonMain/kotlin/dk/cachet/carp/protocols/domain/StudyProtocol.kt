package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.protocols.application.StudyProtocolId
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.configuration.EmptyDeviceConfiguration
import dk.cachet.carp.protocols.domain.configuration.EmptyParticipantDataConfiguration
import dk.cachet.carp.protocols.domain.configuration.EmptyTaskConfiguration
import dk.cachet.carp.protocols.domain.configuration.StudyProtocolComposition
import dk.cachet.carp.protocols.domain.deployment.DeploymentError
import dk.cachet.carp.protocols.domain.deployment.DeploymentIssue
import dk.cachet.carp.protocols.domain.deployment.NoMasterDeviceError
import dk.cachet.carp.protocols.domain.deployment.UnexpectedMeasuresWarning
import dk.cachet.carp.protocols.domain.deployment.UntriggeredTasksWarning
import dk.cachet.carp.protocols.domain.deployment.UnusedDevicesWarning
import dk.cachet.carp.protocols.domain.deployment.UseCompositeTaskWarning


/**
 * A description of how a study is to be executed, defining the type(s) of master device(s) ([AnyMasterDeviceDescriptor]) responsible for aggregating data,
 * the optional devices ([AnyDeviceDescriptor]) connected to them, and the [Trigger]'s which lead to data collection on said devices.
 */
@Suppress( "TooManyFunctions" ) // TODO: some of the device and task configuration methods are overridden solely to add events. Can this be refactored?
class StudyProtocol private constructor( val ownerId: UUID, val name: String, val description: String ) :
    StudyProtocolComposition( EmptyDeviceConfiguration(), EmptyTaskConfiguration(), EmptyParticipantDataConfiguration() )
{
    constructor(
        /**
         * The person or group that created this [StudyProtocol].
         */
        owner: ProtocolOwner,
        /**
         * A unique descriptive name for the protocol assigned by the [ProtocolOwner].
         */
        name: String,
        /**
         * An optional description for the study protocol.
         */
        description: String = ""
    ) : this( owner.id, name, description )


    sealed class Event : DomainEvent()
    {
        data class MasterDeviceAdded( val device: AnyMasterDeviceDescriptor ) : Event()
        data class ConnectedDeviceAdded( val connected: AnyDeviceDescriptor, val master: AnyMasterDeviceDescriptor ) : Event()
        data class TriggerAdded( val trigger: Trigger ) : Event()
        data class TaskAdded( val task: TaskDescriptor ) : Event()
        data class TaskRemoved( val task: TaskDescriptor ) : Event()
        data class TriggeredTaskAdded( val triggeredTask: TriggeredTask ) : Event()
        data class TriggeredTaskRemoved( val triggeredTask: TriggeredTask ) : Event()
        data class ExpectedParticipantDataAdded( val attribute: ParticipantAttribute ) : Event()
        data class ExpectedParticipantDataRemoved( val attribute: ParticipantAttribute ) : Event()
    }


    companion object Factory
    {
        fun fromSnapshot( snapshot: StudyProtocolSnapshot ): StudyProtocol
        {
            val protocol = StudyProtocol( snapshot.id.ownerId, snapshot.id.name, snapshot.description )
            protocol.creationDate = snapshot.creationDate

            // Add master devices.
            snapshot.masterDevices.forEach { protocol.addMasterDevice( it ) }

            // Add connected devices.
            val allDevices: List<AnyDeviceDescriptor> = snapshot.connectedDevices.plus( snapshot.masterDevices ).toList()
            snapshot.connections.forEach { c ->
                val master: AnyMasterDeviceDescriptor = allDevices.filterIsInstance<AnyMasterDeviceDescriptor>().firstOrNull { it.roleName == c.connectedToRoleName }
                    ?: throw IllegalArgumentException( "Can't find master device with role name '${c.connectedToRoleName}' in snapshot." )
                val connected: AnyDeviceDescriptor = allDevices.firstOrNull { it.roleName == c.roleName }
                    ?: throw IllegalArgumentException( "Can't find connected device with role name '${c.roleName}' in snapshot." )
                protocol.addConnectedDevice( connected, master )
            }

            // Add tasks and triggers.
            snapshot.tasks.forEach { protocol.addTask( it ) }
            snapshot.triggers.forEach { protocol.addTrigger( it.value ) }

            // Add triggered tasks.
            snapshot.triggeredTasks.forEach { triggeredTask ->
                val triggerMatch = snapshot.triggers.entries.singleOrNull { it.key == triggeredTask.triggerId }
                    ?: throw IllegalArgumentException( "Can't find trigger with id '${triggeredTask.triggerId}' in snapshot." )
                val task: TaskDescriptor = protocol.tasks.singleOrNull { it.name == triggeredTask.taskName }
                    ?: throw IllegalArgumentException( "Can't find task with name '${triggeredTask.taskName}' in snapshot." )
                val device: AnyDeviceDescriptor = protocol.devices.singleOrNull { it.roleName == triggeredTask.targetDeviceRoleName }
                    ?: throw IllegalArgumentException( "Can't find device with role name '${triggeredTask.targetDeviceRoleName}' in snapshot." )
                protocol.addTriggeredTask( triggerMatch.value, task, device )
            }

            // Add expected participant data.
            snapshot.expectedParticipantData.forEach { protocol.addExpectedParticipantData( it ) }

            // Events introduced by loading the snapshot are not relevant to a consumer wanting to persist changes.
            protocol.consumeEvents()

            return protocol
        }
    }


    /**
     * A study protocol is uniquely identified by the [ownerId] and it's [name].
     */
    val id: StudyProtocolId = StudyProtocolId( ownerId, name )


    /**
     * Add a [masterDevice] which is responsible for aggregating and synchronizing incoming data.
     * Its role name should be unique in the protocol.
     *
     * @throws IllegalArgumentException in case a device with the specified role name already exists.
     * @return True if the [masterDevice] has been added; false if it is already set as a master device.
     */
    override fun addMasterDevice( masterDevice: AnyMasterDeviceDescriptor ): Boolean =
        super.addMasterDevice( masterDevice )
        .eventIf( true ) { Event.MasterDeviceAdded( masterDevice ) }

    /**
     * Add a [device] which is connected to a [masterDevice] within this configuration.
     * Its role name should be unique in the protocol.
     *
     * @throws IllegalArgumentException when:
     *   - a device with the specified role name already exists
     *   - [masterDevice] is not part of the device configuration
     * @return True if the [device] has been added; false if it is already connected to the specified [masterDevice].
     */
    override fun addConnectedDevice( device: AnyDeviceDescriptor, masterDevice: AnyMasterDeviceDescriptor ): Boolean =
        super.addConnectedDevice( device, masterDevice )
        .eventIf( true ) { Event.ConnectedDeviceAdded( device, masterDevice ) }

    private val _triggers: MutableSet<Trigger> = mutableSetOf()

    /**
     * The set of triggers which can trigger tasks in this study protocol.
     */
    val triggers: Set<Trigger>
        get() = _triggers

    private val triggeredTasks: MutableMap<Trigger, MutableSet<TriggeredTask>> = mutableMapOf()

    /**
     * Add a [trigger] to this protocol.
     *
     * @throws IllegalArgumentException when:
     *   - [trigger] does not belong to any device specified in the study protocol
     *   - [trigger] requires a master device and the specified source device is not a master device
     * @return True if the [trigger] has been added; false if the specified [trigger] is already included in this study protocol.
     */
    fun addTrigger( trigger: Trigger ): Boolean
    {
        val device: AnyDeviceDescriptor = deviceConfiguration.devices.firstOrNull { it.roleName == trigger.sourceDeviceRoleName }
            ?: throw IllegalArgumentException( "The passed trigger does not belong to any device specified in this study protocol." )

        if ( trigger.requiresMasterDevice && device !is AnyMasterDeviceDescriptor )
        {
            throw IllegalArgumentException( "The passed trigger cannot be initiated by the specified device since it is not a master device." )
        }

        return _triggers
            .add( trigger )
            .eventIf( true ) {
                triggeredTasks[ trigger ] = mutableSetOf()
                Event.TriggerAdded( trigger )
            }
    }

    /**
     * Add a [task] to be sent to a [targetDevice] once a [trigger] within this protocol is initiated.
     * In case the [trigger] or [task] is not yet included in this study protocol, it will be added.
     * The [targetDevice] needs to be added prior to this call since it needs to be set up as either a master device or connected device.
     *
     * @throws IllegalArgumentException when [targetDevice] is not included in this study protocol.
     * @return True if the [task] to be triggered has been added; false if it is already triggered by the specified [trigger] to the specified [targetDevice].
     */
    fun addTriggeredTask( trigger: Trigger, task: TaskDescriptor, targetDevice: AnyDeviceDescriptor ): Boolean
    {
        // The device needs to be included in the study protocol. We can not add it here since we do not know whether it should be a master or connected device.
        if ( targetDevice !in devices )
        {
            throw IllegalArgumentException( "The passed device to which the task needs to be sent is not included in this study protocol." )
        }

        // Add trigger and task to ensure they are included in the protocol.
        addTrigger( trigger )
        addTask( task )

        // Add triggered task.
        val triggeredTask = TriggeredTask( task, targetDevice )
        return triggeredTasks[ trigger ]!!
            .add( triggeredTask )
            .eventIf( true ) { Event.TriggeredTaskAdded( triggeredTask ) }
    }

    /**
     * Gets all the tasks (and the devices they are triggered to) for the specified [trigger].
     *
     * @throws IllegalArgumentException when [trigger] is not part of this study protocol.
     */
    fun getTriggeredTasks( trigger: Trigger ): Iterable<TriggeredTask>
    {
        if ( trigger !in triggers )
        {
            throw IllegalArgumentException( "The passed trigger is not part of this study protocol." )
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
     * Add a [task] to this configuration.
     *
     * @throws IllegalArgumentException in case a task with the specified name already exists.
     * @return True if the [task] has been added; false if it is already included in this configuration.
     */
    override fun addTask( task: TaskDescriptor ): Boolean =
        super.addTask( task )
        .eventIf( true ) { Event.TaskAdded( task ) }

    /**
     * Remove a [task] currently present in this configuration
     * including removing it from any [Trigger]'s which initiate it.
     *
     * @return True if the [task] has been removed; false if it is not included in this configuration.
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
        return taskConfiguration
            .removeTask( task )
            .eventIf( true ) { Event.TaskRemoved( task ) }
    }

    /**
     * Add expected participant data [attribute] to be be input by users.
     *
     * @throws IllegalArgumentException in case a differing [attribute] with a matching input type is already added.
     * @return True if the [attribute] has been added; false in case the same [attribute] has already been added before.
     */
    override fun addExpectedParticipantData( attribute: ParticipantAttribute ): Boolean =
        super.addExpectedParticipantData( attribute )
        .eventIf( true ) { Event.ExpectedParticipantDataAdded( attribute ) }

    /**
     * Remove expected participant data [attribute] to be input by users.
     *
     * @return True if the [attribute] has been removed; false if it is not included in this configuration.
     */
    override fun removeExpectedParticipantData( attribute: ParticipantAttribute ): Boolean =
        super.removeExpectedParticipantData( attribute )
        .eventIf( true ) { Event.ExpectedParticipantDataRemoved( attribute ) }

    /**
     * Replace the expected participant data to be input by users with the specified [attributes].
     *
     * TODO: This is currently defined in `StudyProtocol` rather than `ParticipantDataConfiguration` due to the need to track events.
     *   Once eventing is implemented on `ParticipantDataConfiguration`, this can be moved where it logically belongs.
     *
     * @throws IllegalArgumentException in case the specified [attributes] contain two or more attributes with the same input type.
     * @return True if any attributes have been replaced; false if the specified [attributes] were the same as those already set.
     */
    fun replaceExpectedParticipantData( attributes: Set<ParticipantAttribute> ): Boolean
    {
        require( attributes.map { it.inputType }.toSet().size == attributes.size )
            { "The specified attributes contain two or more attributes with the same input type." }

        val toRemove = expectedParticipantData.minus( attributes )
        val toAdd = attributes.minus( expectedParticipantData )

        if ( toRemove.isEmpty() && toAdd.isEmpty() ) return false

        toRemove.forEach { removeExpectedParticipantData( it ) }
        toAdd.forEach { addExpectedParticipantData( it ) }
        return true
    }


    /**
     * All possible issues related to incomplete or problematic configuration of a [StudyProtocol] which might prevent deployment.
     */
    private val possibleDeploymentIssues: List<DeploymentIssue> = listOf(
        NoMasterDeviceError(),
        UntriggeredTasksWarning(),
        UseCompositeTaskWarning(),
        UnusedDevicesWarning(),
        UnexpectedMeasuresWarning()
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
