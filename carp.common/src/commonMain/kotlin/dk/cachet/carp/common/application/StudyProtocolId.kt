package dk.cachet.carp.common.application

import kotlinx.serialization.Serializable


/**
 * Uniquely identifies a study protocol by the [ownerId] and it's [name].
 */
@Serializable
data class StudyProtocolId( val ownerId: UUID, val name: String )
