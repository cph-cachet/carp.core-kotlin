package dk.cachet.carp.protocols.application.users

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.users.ParticipantAttribute
import kotlinx.serialization.Serializable


/**
 * Describes a participant [attribute] to be [inputBy] specified users in a study.
 */
@Serializable
data class ExpectedParticipantData( val attribute: ParticipantAttribute, val inputBy: InputBy = InputBy.Anyone )
{
    @Serializable
    sealed class InputBy
    {
        @Serializable
        object Anyone : InputBy()

        @Serializable
        class Roles( val roleNames: Set<String> ) : InputBy()
    }

    val inputDataType: InputDataType
        get() = attribute.inputDataType
}


/**
 * Determines whether input [data] for a given [inputDataType],
 * as registered in [registeredInputDataTypes] or of type [CustomInput],
 * is expected to be input by the participant with role [inputByRole] (null if any) and is valid.
 */
fun <TData : Data?> Set<ExpectedParticipantData>.isValidParticipantData(
    registeredInputDataTypes: InputDataTypeList,
    inputDataType: InputDataType,
    data: TData,
    /**
     * The role of the participant who input [data]; null if any.
     */
    inputByRole: String? = null
): Boolean
{
    val attributesForRole = this.filter {
        when ( it.inputBy )
        {
            is ExpectedParticipantData.InputBy.Anyone -> true
            is ExpectedParticipantData.InputBy.Roles -> inputByRole in it.inputBy.roleNames
        }
    }

    val expectedData = attributesForRole.firstOrNull { it.inputDataType == inputDataType } ?: return false
    val attribute = expectedData.attribute

    return attribute.isValidData( registeredInputDataTypes, data )
}
