package dk.cachet.carp.common.application.users

import kotlinx.serialization.*


/**
 * Describes a participant playing a [role] in a study, and whether this role [isOptional].
 */
@Serializable
data class ParticipantRole( val role: String, val isOptional: Boolean )
