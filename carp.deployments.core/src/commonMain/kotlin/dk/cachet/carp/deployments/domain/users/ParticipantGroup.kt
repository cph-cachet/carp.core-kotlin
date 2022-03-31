package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.hasNoConflicts
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


/**
 * A group of participants participating in a study deployment using the [assignedPrimaryDevices].
 * Consent and participant data is managed here.
 *
 * TODO: Implement consent.
 */
class ParticipantGroup private constructor(
    val studyDeploymentId: UUID,
    assignedPrimaryDevices: Set<AssignedPrimaryDevice>,
    val expectedData: Set<ExpectedParticipantData>,
    id: UUID = UUID.randomUUID(),
    createdOn: Instant = Clock.System.now()
) : AggregateRoot<ParticipantGroup, ParticipantGroupSnapshot, ParticipantGroup.Event>( id, createdOn )
{
    sealed class Event : DomainEvent
    {
        data class DataSet( val inputDataType: InputDataType, val data: Data? ) : Event()
        data class ParticipationAdded( val accountParticipation: AccountParticipation ) : Event()
        data class DeviceRegistrationChanged( val assignedPrimaryDevice: AssignedPrimaryDevice ) : Event()
        object StudyDeploymentStopped : Event()
    }


    init { expectedData.hasNoConflicts( exceptionOnConflict = true ) }

    companion object
    {
        /**
         * Initialize a [ParticipantGroup] with default values for a newly created deployment with [studyDeploymentId]
         * which was just created for the specified study [protocol].
         */
        fun fromNewDeployment( studyDeploymentId: UUID, protocol: StudyProtocol ): ParticipantGroup =
            ParticipantGroup(
                studyDeploymentId,
                protocol.primaryDevices.map { AssignedPrimaryDevice( it ) }.toSet(),
                protocol.expectedParticipantData
            )

        /**
         * Initialize a [ParticipantGroup] with default values for a newly created [deployment].
         */
        fun fromNewDeployment( deployment: StudyDeployment ): ParticipantGroup =
            fromNewDeployment( deployment.id, deployment.protocol )

        fun fromSnapshot( snapshot: ParticipantGroupSnapshot ): ParticipantGroup
        {
            val group = ParticipantGroup(
                snapshot.studyDeploymentId,
                snapshot.assignedPrimaryDevices,
                snapshot.data.keys,
                snapshot.id,
                snapshot.createdOn
            )
            group.isStudyDeploymentStopped = snapshot.isStudyDeploymentStopped

            // Add participations.
            snapshot.participations.forEach { p -> group._participations.add( p.copy() ) }

            // Add participant data.
            snapshot.data.forEach { (expectedData, data) ->
                group._data[ expectedData ] = data
            }

            return group
        }
    }


    /**
     * The account IDs participating in this study group and the pseudonym IDs assigned to them.
     */
    val participations: Set<AccountParticipation>
        get() = _participations

    private val _participations: MutableSet<AccountParticipation> = mutableSetOf()

    /**
     * Specify that an [account] (invited using [invitation]) participates in this group, identified by [participation],
     * and was requested to use the [assignedPrimaryDevices].
     *
     * @throws IllegalArgumentException if [participation] is already added to this participant group,
     * or if the [participation] details do not match the study deployment of this participant group.
     * @throws IllegalStateException when the study deployment of this participant group has stopped.
     */
    fun addParticipation(
        account: Account,
        invitation: StudyInvitation,
        participation: Participation,
        assignedPrimaryDevices: Set<AnyPrimaryDeviceConfiguration>
    )
    {
        require( studyDeploymentId == participation.studyDeploymentId )
            { "The specified participation details do not match the study deployment of this participant group." }
        require( _participations.none { it.participation.participantId == participation.participantId } )
            { "The specified participant ID is already added to this study deployment." }
        check( !isStudyDeploymentStopped )

        val assignedPrimaryDeviceRoleNames = assignedPrimaryDevices.map { it.roleName }.toSet()
        val accountParticipation = AccountParticipation(
            participation,
            assignedPrimaryDeviceRoleNames,
            account.id,
            invitation
        )
        _participations.add( accountParticipation )
        event( Event.ParticipationAdded( accountParticipation ) )
    }

    /**
     * The assigned primary devices to participants in this group and their device registrations, if any.
     */
    val assignedPrimaryDevices: Set<AssignedPrimaryDevice>
        get() = _assignedPrimaryDevices

    private val _assignedPrimaryDevices: MutableSet<AssignedPrimaryDevice> = assignedPrimaryDevices.toMutableSet()

    /**
     * Return the [AssignedPrimaryDevice] with the specified [roleName].
     *
     * @throws IllegalArgumentException when no assigned device with [roleName] exists for this participant group.
     */
    fun getAssignedPrimaryDevice( roleName: String ) =
        assignedPrimaryDevices.firstOrNull { it.device.roleName == roleName }
            ?: throw IllegalArgumentException( "There is no assigned device with role name \"$roleName\" for this participant group." )

    /**
     * Update the device [registration] for the given assigned [primaryDevice].
     *
     * @throws IllegalArgumentException when [primaryDevice] is not part of this participant group.
     */
    fun updateDeviceRegistration( primaryDevice: AnyPrimaryDeviceConfiguration, registration: DeviceRegistration? )
    {
        val assignedDevice = _assignedPrimaryDevices.firstOrNull { it.device == primaryDevice }
        requireNotNull( assignedDevice ) { "The passed primary device is not part of this participant group." }

        if ( assignedDevice.registration != registration )
        {
            _assignedPrimaryDevices.remove( assignedDevice )
            val updatedRegistrationDevice = assignedDevice.copy( registration = registration )
            _assignedPrimaryDevices.add( updatedRegistrationDevice )
            event( Event.DeviceRegistrationChanged( updatedRegistrationDevice ) )
        }
    }

    /**
     * Determines whether the study deployment of this participant group has been stopped
     * and no further participations can be added
     */
    var isStudyDeploymentStopped: Boolean = false
        private set

    fun studyDeploymentStopped()
    {
        if ( !isStudyDeploymentStopped )
        {
            isStudyDeploymentStopped = true
            event( Event.StudyDeploymentStopped )
        }
    }

    /**
     * Data pertaining to participants in this group which is input by users.
     */
    val data: Map<ExpectedParticipantData, Data?>
        get() = _data

    private val _data: MutableMap<ExpectedParticipantData, Data?> =
        // All expected participant data is null by default.
        expectedData.associateWith { null }.toMutableMap()

    /**
     * Set [data] [assignedToParticipantRole] for the given [inputDataType], or unset if [data] is `null`,
     * using [registeredInputDataTypes] to verify whether the data is valid for default input data types.
     *
     * @throws IllegalArgumentException when:
     *   - [inputDataType] is not configured as expected participant data [assignedToParticipantRole]
     *   - [data] is invalid data for [inputDataType]
     * @return True when data changed; false when data was already set.
     */
    fun setData(
        registeredInputDataTypes: InputDataTypeList,
        inputDataType: InputDataType,
        data: Data?,
        /**
         * The participant role [data] was assigned to; null if anyone can set the specified [inputDataType].
         */
        assignedToParticipantRole: String? = null
    ): Boolean
    {
        val dataToSet = getExpectedDataOrThrow( registeredInputDataTypes, inputDataType, data, assignedToParticipantRole )

        val prevData = _data.put( dataToSet, data )

        return ( prevData != data )
            .eventIf( true ) { Event.DataSet( inputDataType, data ) }
    }

    /**
     * Set [data] for the participants in this group, or unset it by passing `null`,
     * using [registeredInputDataTypes] to verify whether the data is valid for default input data types.
     *
     * @throws IllegalArgumentException when:
     *   - one or more of the keys in [data] isn't configured as expected participant data
     *   - one or more of the set [data] isn't valid for the corresponding input data type
     * @return True when any data has changed; false when all [data] was already set.
     */
    fun setData(
        registeredInputDataTypes: InputDataTypeList,
        data: Map<InputDataType, Data?>,
        /**
         * The participant role who filled out [data]; null if anyone
         */
        inputByParticipantRole: String? = null
    ): Boolean
    {
        data.forEach { getExpectedDataOrThrow( registeredInputDataTypes, it.key, it.value, inputByParticipantRole ) }

        return data.entries.fold( false ) { anyDataChanged, element ->
            setData( registeredInputDataTypes, element.key, element.value ) || anyDataChanged
        }
    }

    /**
     * Returns the matching [ExpectedParticipantData], but only if the data input is valid; throws otherwise.
     */
    private fun getExpectedDataOrThrow(
        registeredInputDataTypes: InputDataTypeList,
        inputDataType: InputDataType,
        data: Data?,
        assignedToParticipantRole: String?
    ): ExpectedParticipantData
    {
        val dataToSet = expectedData
            .filter {
                when ( val assignedTo = it.assignedTo )
                {
                    is AssignedTo.Anyone -> true
                    is AssignedTo.Roles -> assignedToParticipantRole in assignedTo.roleNames
                }
            }
            .firstOrNull { it.inputDataType == inputDataType }
        requireNotNull( dataToSet ) { "The input data type is not assigned to this participant role." }
        require( dataToSet.attribute.isValidData( registeredInputDataTypes, data ) ) { "Invalid data is passed" }

        return dataToSet
    }

    /**
     * Get a serializable snapshot of the current state of this [ParticipantGroup].
     */
    override fun getSnapshot(): ParticipantGroupSnapshot = ParticipantGroupSnapshot.fromParticipantGroup( this )
}
