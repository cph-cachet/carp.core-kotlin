package dk.cachet.carp.common.application.data.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds the biological sex assigned at birth of a participant.
 */
@Serializable
@SerialName( CarpInputDataTypes.SEX_TYPE_NAME )
enum class Sex : Data
{
    Male,
    Female,
    Intersex
}
