package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole


/**
 * Configures expected participants and data to be input by users.
 */
interface ProtocolParticipantConfiguration
{
    /**
     * Roles which can be assigned to participants in the study and [ParticipantAttribute]s can be linked to.
     * If a [ParticipantAttribute] is not linked to any specific participant role,
     * the participant data can be filled out by all participants in the study deployment.
     */
    val participantRoles: Set<ParticipantRole>

    /**
     * Data about participants in a study protocol, expected to be input by users.
     */
    val expectedParticipantData: Set<ExpectedParticipantData>


    /**
     * Add a participant role which can be assigned to participants in the study.
     *
     * @throws IllegalArgumentException in case a differing [role] with a matching role name is already added.
     * @return True if the [role] has been added; false in case the same [role] has already been added before.
     */
    fun addParticipantRole( role: ParticipantRole ): Boolean

    /**
     * Determines whether all participant roles in [assignment] are part of this configuration.
     */
    fun isValidAssignment( assignment: AssignedTo ): Boolean =
        when ( assignment )
        {
            is AssignedTo.All -> true
            is AssignedTo.Roles ->
            {
                val roles = participantRoles.map { it.role }
                assignment.roleNames.all { it in roles }
            }
        }

    /**
     * Add expected participant data to be input by users.
     *
     * @throws IllegalArgumentException if:
     *  - [expectedData] is assigned to a participant role which is not part of this [ProtocolParticipantConfiguration]
     *  - a differing [ParticipantAttribute] with a matching input data type is already added
     *  - [expectedParticipantData] already contains an input data type which can be input by the same role
     * @return True if the [expectedData] has been added; false in case the same [expectedData] has already been added before.
     */
    fun addExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean

    /**
     * Remove expected participant data to be input by users.
     *
     * @return True if the [expectedData] has been removed; false if it is not included in this configuration.
     */
    fun removeExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean
}
