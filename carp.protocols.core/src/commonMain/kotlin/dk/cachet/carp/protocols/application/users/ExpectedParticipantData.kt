package dk.cachet.carp.protocols.application.users

import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.users.ParticipantAttribute
import kotlinx.serialization.Serializable


/**
 * Describes a participant [attribute] to be input by specified users in a study.
 */
@Serializable
data class ExpectedParticipantData( val attribute: ParticipantAttribute )
{
    val inputDataType: InputDataType
        get() = attribute.inputDataType
}
