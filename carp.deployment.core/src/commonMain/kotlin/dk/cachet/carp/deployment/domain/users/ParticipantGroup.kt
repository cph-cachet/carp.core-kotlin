package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.isValidParticipantData


/**
 * A group of participants participating in a study deployment using the [assignedMasterDevices].
 * Consent and participant data is managed here.
 *
 * TODO: Implement consent.
 */
class ParticipantGroup private constructor(
    val studyDeploymentId: UUID,
    assignedMasterDevices: Set<AssignedMasterDevice>,
    val expectedData: Set<ParticipantAttribute>
) : AggregateRoot<ParticipantGroup, ParticipantGroupSnapshot, ParticipantGroup.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class DataSet( val inputDataType: InputDataType, val data: Data? ) : Event()
        data class ParticipationAdded( val accountParticipation: AccountParticipation ) : Event()
        data class DeviceRegistrationChanged( val assignedMasterDevice: AssignedMasterDevice ) : Event()
        object StudyDeploymentStopped : Event()
    }

    companion object
    {
        /**
         * Initialize a [ParticipantGroup] with default values for a study [deployment].
         */
        fun fromDeployment( deployment: StudyDeployment ): ParticipantGroup =
            ParticipantGroup(
                deployment.id,
                deployment.protocol.masterDevices.map { AssignedMasterDevice( it, null ) }.toSet(),
                deployment.protocol.expectedParticipantData )

        fun fromSnapshot( snapshot: ParticipantGroupSnapshot ): ParticipantGroup
        {
            val group = ParticipantGroup( snapshot.studyDeploymentId, snapshot.assignedMasterDevices, snapshot.expectedData )
            group.isStudyDeploymentStopped = snapshot.isStudyDeploymentStopped
            group.creationDate = snapshot.creationDate

            // Add participations.
            snapshot.participations.forEach { p -> group._participations.add( p.copy() ) }

            // Add participant data.
            snapshot.data.forEach { (inputType, data) ->
                group._data[ inputType ] = data
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
     * Let an [account] participate in the deployment of this participant group.
     *
     * @throws IllegalArgumentException if the specified [account] already participates in this participant group,
     * or if the [participation] details do not match the study deployment of this participant group.
     * @throws IllegalStateException when the study deployment of this participant group has stopped.
     */
    fun addParticipation(
        account: Account,
        participation: Participation,
        invitation: StudyInvitation,
        assignedMasterDevices: Set<AnyMasterDeviceDescriptor>
    )
    {
        require( studyDeploymentId == participation.studyDeploymentId )
            { "The specified participation details do not match the study deployment of this participant group." }
        require( _participations.none { it.accountId == account.id } )
            { "The specified account already participates in this study deployment." }
        check( !isStudyDeploymentStopped )

        val assignedMasterDeviceRoleNames = assignedMasterDevices.map { it.roleName }.toSet()
        val accountParticipation = AccountParticipation(
            account.id,
            Participation( studyDeploymentId, participation.id ),
            invitation,
            assignedMasterDeviceRoleNames )
        _participations.add( accountParticipation )
        event( Event.ParticipationAdded( accountParticipation ) )
    }

    /**
     * Get the participation details for a given [account] in this study group,
     * or null in case the [account] does not participate in this study group.
     */
    fun getParticipation( account: Account ): Participation? =
        _participations
            .filter { it.accountId == account.id }
            .map { it.participation }
            .singleOrNull()

    /**
     * The assigned master devices to participants in this group and their device registrations, if any.
     */
    val assignedMasterDevices: Set<AssignedMasterDevice>
        get() = _assignedMasterDevices

    private val _assignedMasterDevices: MutableSet<AssignedMasterDevice> = assignedMasterDevices.toMutableSet()

    /**
     * Return the [AssignedMasterDevice] with the specified [roleName].
     *
     * @throws IllegalArgumentException when no assigned device with [roleName] exists for this participant group.
     */
    fun getAssignedMasterDevice( roleName: String ) =
        assignedMasterDevices.firstOrNull { it.device.roleName == roleName }
            ?: throw IllegalArgumentException( "There is no assigned device with role name \"$roleName\" for this participant group." )

    /**
     * Update the device [registration] for the given assigned [masterDevice].
     *
     * @throws IllegalArgumentException when [masterDevice] is not part of this participant group.
     */
    fun updateDeviceRegistration( masterDevice: AnyMasterDeviceDescriptor, registration: DeviceRegistration? )
    {
        val assignedDevice = _assignedMasterDevices.firstOrNull { it.device == masterDevice }
        requireNotNull( assignedDevice ) { "The passed master device is not part of this participant group." }

        if ( assignedDevice.registration != registration )
        {
            _assignedMasterDevices.remove( assignedDevice )
            val updatedRegistrationDevice = assignedDevice.copy( registration = registration )
            _assignedMasterDevices.add( updatedRegistrationDevice )
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
    val data: Map<InputDataType, Data?>
        get() = _data

    private val _data: MutableMap<InputDataType, Data?> =
        // All expected participant data is null by default.
        expectedData.map { it.inputType to null }.toMap().toMutableMap()

    /**
     * Set [data] for the participants in this group for the given [inputDataType],
     * using [registeredInputDataTypes] to verify whether the data is valid for default input data types.
     *
     * @throws IllegalArgumentException when:
     *   - [inputDataType] is not configured as expected participant data
     *   - [data] is invalid data for [inputDataType]
     */
    fun setData( registeredInputDataTypes: InputDataTypeList, inputDataType: InputDataType, data: Data? )
    {
        require( expectedData.isValidParticipantData( registeredInputDataTypes, inputDataType, data ) )
            { "The input data type is not expected or invalid data is passed." }

        val prevData = _data.put( inputDataType, data )

        if ( prevData != data ) event( Event.DataSet( inputDataType, data ) )
    }

    /**
     * Get a serializable snapshot of the current state of this [ParticipantGroup].
     */
    override fun getSnapshot(): ParticipantGroupSnapshot = ParticipantGroupSnapshot.fromParticipantGroup( this )
}
