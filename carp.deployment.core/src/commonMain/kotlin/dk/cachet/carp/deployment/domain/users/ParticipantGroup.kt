package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.InputDataTypeList
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.isValidParticipantData


/**
 * A group of participants participating in a study deployment.
 * Consent and participant data is managed here.
 *
 * TODO: Implement consent.
 */
class ParticipantGroup private constructor( val studyDeploymentId: UUID, val expectedData: Set<ParticipantAttribute> ) :
    AggregateRoot<ParticipantGroup, ParticipantGroupSnapshot, ParticipantGroup.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class DataSet( val inputDataType: InputDataType, val data: Data? ) : Event()
        data class ParticipationAdded( val accountParticipation: AccountParticipation ) : Event()
    }

    companion object
    {
        /**
         * Initialize a [ParticipantGroup] with default values for a study [deployment].
         */
        fun fromDeployment( deployment: StudyDeployment ): ParticipantGroup =
            ParticipantGroup( deployment.id, deployment.protocol.expectedParticipantData )

        fun fromSnapshot( snapshot: ParticipantGroupSnapshot ): ParticipantGroup
        {
            val group = ParticipantGroup( snapshot.studyDeploymentId, snapshot.expectedData )
            group.creationDate = snapshot.creationDate

            // Add participations.
            snapshot.participations.forEach { p ->
                group._participations.add( AccountParticipation( p.accountId, p.participationId ) )
            }

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
     */
    fun addParticipation( account: Account, participation: Participation )
    {
        require( studyDeploymentId == participation.studyDeploymentId )
            { "The specified participation details do not match the study deployment of this participant group." }
        require( _participations.none { it.accountId == account.id } )
            { "The specified account already participates in this study deployment." }

        val accountParticipation = AccountParticipation( account.id, participation.id )
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
            .map { Participation( studyDeploymentId, it.participationId ) }
            .singleOrNull()

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
