package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies a study protocol by the [ownerId] and it's [name].
 */
@Serializable
data class StudyProtocolId( val ownerId: UUID, val name: String )
