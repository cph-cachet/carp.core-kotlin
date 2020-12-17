package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.CustomInput
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.InputDataTypeList
import dk.cachet.carp.common.users.ParticipantAttribute


/**
 * Configures data expected to be input by users, related to the expected participants in a [StudyProtocol].
 */
interface ParticipantDataConfiguration
{
    /**
     * Data about participants in a [StudyProtocol], expected to be input by users.
     */
    val expectedParticipantData: Set<ParticipantAttribute>


    /**
     * Add expected participant data [attribute] to be be input by users.
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
    val attribute = this.firstOrNull { it.inputType == inputDataType } ?: return false

    return attribute.isValidData( registeredInputDataTypes, data )
}
