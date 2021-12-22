@file:JsExport

package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Holds text of which the interpretation is left up to the specific application.
 */
@Serializable
@SerialName( CarpDataTypes.FREE_FORM_TEXT_TYPE_NAME )
data class FreeFormText( val text: String ) : Data
