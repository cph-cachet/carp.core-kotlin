package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds text of which the interpretation is left up to the specific application.
 */
@Serializable
@SerialName( CarpDataTypes.FREEFORMTEXT_TYPE_NAME )
data class FreeFormText( val text: String ) : Data
