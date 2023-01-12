package dk.cachet.carp.common.application.users

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Describes a participant playing a [role] in a study, and whether this role [isOptional].
 */
@Serializable
@JsExport
data class ParticipantRole( val role: String, val isOptional: Boolean )
