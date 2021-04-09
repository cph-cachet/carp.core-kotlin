package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable


/**
 * A person or group that created a [StudyProtocol].
 */
@Serializable
data class ProtocolOwner( val id: UUID = UUID.randomUUID() )
