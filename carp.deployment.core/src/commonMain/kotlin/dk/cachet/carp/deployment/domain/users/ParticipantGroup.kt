package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.InputDataTypeList
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent
import dk.cachet.carp.common.users.ParticipantAttribute
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.isValidParticipantData


/**
 * A group of participants participating in a study deployment.
 * Consent and participant data is managed here.
 *
 * TODO: Implement consent.
 * TODO: Participation management should be moved from `StudyDeployment` to here.
 *   This will require updating diagrams in the documentation.
 */
class ParticipantGroup private constructor( val studyDeploymentId: UUID, val expectedData: Set<ParticipantAttribute> ) :
    AggregateRoot<ParticipantGroup, ParticipantGroupSnapshot, ParticipantGroup.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class DataSet( val inputDataType: InputDataType, val data: Data? ) : Event()
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
            snapshot.data.forEach { (inputType, data) ->
                group._data[ inputType ] = data
            }

            return group
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
