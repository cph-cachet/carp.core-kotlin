package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies the person or group that created a [StudyProtocol].
 */
@Serializable
data class ProtocolOwner( val id: UUID = UUID.randomUUID() )
