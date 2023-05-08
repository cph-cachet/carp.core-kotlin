package dk.cachet.carp.common.application.data.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Holds the biological sex assigned at birth of a participant.
 */
@Serializable
@SerialName( CarpInputDataTypes.SEX_TYPE_NAME )
@JsExport
enum class Sex : Data
{
    Male,
    Female,
    Intersex
}
