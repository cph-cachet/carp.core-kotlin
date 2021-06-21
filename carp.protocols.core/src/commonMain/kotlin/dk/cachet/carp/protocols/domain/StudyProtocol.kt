package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.application.triggers.TaskControl.Control as Control
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
import dk.cachet.carp.protocols.domain.deployment.UnstartedTasksWarning
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
        data class TaskControlAdded( val control: TaskControl ) : Event()
        data class TaskControlRemoved( val control: TaskControl ) : Event()
        data class ExpectedParticipantDataAdded( val attribute: ParticipantAttribute ) : Event()
        data class ExpectedParticipantDataRemoved( val attribute: ParticipantAttribute ) : Event()
    }


    companion object Factory
    {
        fun fromSnapshot( snapshot: StudyProtocolSnapshot ): StudyProtocol
        {
            val protocol = StudyProtocol( snapshot.id.ownerId, snapshot.id.name, snapshot.description ).apply {
                creationDate = snapshot.creationDate
                applicationData = snapshot.applicationData
            }

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

            // Add tasks.
            snapshot.tasks.forEach { protocol.addTask( it ) }

            // Add triggers.
            val triggerIds = snapshot.triggers.keys.sorted()
            if ( triggerIds.isNotEmpty() )
            {
                require( triggerIds.first() == 0 && triggerIds.last() == triggerIds.size - 1 )
                    { "Triggers should be given sequential IDs starting with 0." }
                triggerIds.map { protocol.addTrigger( snapshot.triggers[ it ]!! ) }
            }

            // Add task controls.
            snapshot.taskControls.forEach { control ->
                val triggerMatch = snapshot.triggers.entries.singleOrNull { it.key == control.triggerId }
                    ?: throw IllegalArgumentException( "Can't find trigger with id '${control.triggerId}' in snapshot." )
                val task: TaskDescriptor = protocol.tasks.singleOrNull { it.name == control.taskName }
                    ?: throw IllegalArgumentException( "Can't find task with name '${control.taskName}' in snapshot." )
                val device: AnyDeviceDescriptor = protocol.devices.singleOrNull { it.roleName == control.destinationDeviceRoleName }
                    ?: throw IllegalArgumentException( "Can't find device with role name '${control.destinationDeviceRoleName}' in snapshot." )
                protocol.addTaskControl( triggerMatch.value, task, device, control.control )
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

    /**
     * Set of triggers in the exact sequence by which they were added to the protocol.
     * A `LinkedHashSet` is used to guarantee this order is maintained, allowing to use the index as ID.
     */
    private val _triggers: LinkedHashSet<Trigger> = LinkedHashSet()

    /**
     * The list of triggers with assigned IDs which can start or stop tasks in this study protocol.
     */
    val triggers: List<TriggerWithId>
        get() = _triggers.mapIndexed { index, trigger -> TriggerWithId( index, trigger ) }

    /**
     * Stores which tasks need to be started or stopped when the conditions defined by [triggers] are met.
     */
    private val triggerControls: MutableMap<Trigger, MutableSet<TaskControl>> = mutableMapOf()

    /**
     * Add a [trigger] to this protocol.
     *
     * @throws IllegalArgumentException when:
     *   - [trigger] does not belong to any device specified in the study protocol
     *   - [trigger] requires a master device and the specified source device is not a master device
     * @return The [trigger] and its newly assigned ID, or previously assigned ID in case the trigger is already included in this protocol.
     */
    fun addTrigger( trigger: Trigger ): TriggerWithId
    {
        val device: AnyDeviceDescriptor = deviceConfiguration.devices.firstOrNull { it.roleName == trigger.sourceDeviceRoleName }
            ?: throw IllegalArgumentException( "The passed trigger does not belong to any device specified in this study protocol." )

        if ( trigger.requiresMasterDevice && device !is AnyMasterDeviceDescriptor )
        {
            throw IllegalArgumentException( "The passed trigger cannot be initiated by the specified device since it is not a master device." )
        }

        val isAdded = _triggers.add( trigger )
        if ( isAdded )
        {
            triggerControls[ trigger ] = mutableSetOf()
            event( Event.TriggerAdded( trigger ) )
        }

        return TriggerWithId( _triggers.indexOf( trigger ), trigger )
    }

    /**
     * Add a [task] to be started or stopped (determined by [control]) on a [destinationDevice]
     * once a [trigger] within this protocol is initiated.
     * In case the [trigger] or [task] is not yet included in this study protocol, it will be added.
     * The [destinationDevice] needs to be added prior to this call since it needs to be set up as
     * either a master device or connected device.
     *
     * @throws IllegalArgumentException when [destinationDevice] is not included in this study protocol.
     * @return True if the task control has been added; false if the same control is already present.
     */
    fun addTaskControl(
        trigger: Trigger,
        task: TaskDescriptor,
        destinationDevice: AnyDeviceDescriptor,
        control: Control
    ): Boolean
    {
        // The device needs to be included in the study protocol.
        // We cannot add it here since we do not know whether it should be a master or connected device.
        require( destinationDevice in devices )
            { "The passed device to which the task needs to be sent is not included in this study protocol." }

        // Add trigger and task to ensure they are included in the protocol.
        addTrigger( trigger )
        addTask( task )

        // Add task control.
        val taskControl = TaskControl( trigger, task, destinationDevice, control )
        return triggerControls[ trigger ]!!
            .add( taskControl )
            .eventIf( true ) { Event.TaskControlAdded( taskControl ) }
    }

    /**
     * Add a task to be started or stopped on a device once a trigger within this protocol is initiated.
     * In case the trigger or task defined in [control] is not yet included in this study protocol, it will be added.
     * The destination device defined in [control] needs to be added prior to this call since it needs to be set up as
     * either a master device or connected device.
     *
     * @throws IllegalArgumentException when the destination device is not included in this study protocol.
     * @return True if the task control has been added; false if the same control is already present.
     */
    fun addTaskControl( control: TaskControl ): Boolean =
        addTaskControl( control.trigger, control.task, control.destinationDevice, control.control )

    /**
     * Gets all conditions which control that tasks get started or stopped on devices in this protocol by the specified [trigger].
     *
     * @throws IllegalArgumentException when [trigger] is not part of this study protocol.
     */
    fun getTaskControls( trigger: Trigger ): Iterable<TaskControl>
    {
        require( trigger in _triggers ) { "The passed trigger is not part of this study protocol." }

        return triggerControls[ trigger ]!!
    }

    /**
     * Gets all conditions which control that tasks get started or stopped on devices in this protocol
     * by the trigger with [triggerId].
     *
     * @throws IllegalArgumentException when a trigger with [triggerId] is not defined in this study protocol.
     */
    fun getTaskControls( triggerId: Int ): Iterable<TaskControl>
    {
        val trigger = triggers.firstOrNull { it.id == triggerId }?.trigger
        requireNotNull( trigger ) { "There is no trigger with ID \"$triggerId\" in this study protocol." }

        return triggerControls[ trigger ]!!
    }

    /**
     * Gets all the tasks triggered for the specified [device].
     */
    fun getTasksForDevice( device: AnyDeviceDescriptor ): Set<TaskDescriptor>
    {
        return triggerControls
            .flatMap { it.value }
            .filter { it.destinationDevice == device }
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
        // Remove all controls which control this task.
        triggerControls.values.forEach { controls ->
            val taskControls = controls.filter { it.task == task }
            controls.removeAll( taskControls )
            taskControls.forEach { event( Event.TaskControlRemoved( it ) ) }
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
     * Application-specific data to be stored as part of the study protocol
     * which will be included in all deployments of this study protocol.
     *
     * This can be used by infrastructures or concrete applications which require exchanging additional data
     * between the protocols and clients subsystems, outside of scope or not yet supported by CARP core.
     */
    var applicationData: String = ""


    /**
     * All possible issues related to incomplete or problematic configuration of a [StudyProtocol] which might prevent deployment.
     */
    private val possibleDeploymentIssues: List<DeploymentIssue> = listOf(
        NoMasterDeviceError(),
        UnstartedTasksWarning(),
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
