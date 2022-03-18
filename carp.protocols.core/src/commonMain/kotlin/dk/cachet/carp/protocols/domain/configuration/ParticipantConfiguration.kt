package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole


/**
 * Configures expected participants and data to be input by users.
 */
interface ParticipantConfiguration
{
    /**
     * Roles which can be assigned to participants in the study and [ParticipantAttribute]s can be linked to.
     * If a [ParticipantAttribute] is not linked to any participant role,
     * the participant data can be filled out by anyone in the study deployment.
     */
    val participantRoles: Set<ParticipantRole>

    /**
     * Data about participants in a study protocol, expected to be input by users.
     */
    val expectedParticipantData: Set<ParticipantAttribute>


    /**
     * Add a participant role which can be assigned to participants in the study.
     *
     * @throws IllegalArgumentException in case a differing [role] with a matching role name is already added.
     * @return True if the [role] has been added; false in case the same [role] has already been added before.
     */
    fun addParticipantRole( role: ParticipantRole ): Boolean

    /**
     * Remove a participant role which can be assigned to participants in the study,
     * as well as all [ParticipantAttribute]s linked to it.
     *
     * @return True if the [role] and linked attributes have been removed;
     *   false if the role is not included in this configuration.
     */
    fun removeParticipantRole( role: ParticipantRole ): Boolean

    /**
     * Add expected participant data [attribute] to be input by users.
     *
     * @throws IllegalArgumentException in case a differing [attribute] with a matching input type is already added.
     * @return True if the [attribute] has been added; false in case the same [attribute] has already been added before.
     */
    fun addExpectedParticipantData( attribute: ParticipantAttribute ): Boolean

    /**
     * Remove expected participant data [attribute] to be input by users.
     *
     * @return True if the [attribute] has been removed; false if it is not included in this configuration.
     */
    fun removeExpectedParticipantData( attribute: ParticipantAttribute ): Boolean
}


/**
 * Determines whether input [data] for a given [inputDataType],
 * as registered in [registeredInputDataTypes] or of type [CustomInput],
 * is expected to be input by users and is valid.
 */
fun <TData : Data?> Set<ParticipantAttribute>.isValidParticipantData(
    registeredInputDataTypes: InputDataTypeList,
    inputDataType: InputDataType,
    data: TData
): Boolean
{
    val attribute = this.firstOrNull { it.inputDataType == inputDataType } ?: return false

    return attribute.isValidData( registeredInputDataTypes, data )
}
