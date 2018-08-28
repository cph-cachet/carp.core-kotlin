package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.common.UUID


/**
 * Uniquely identifies the person or group that created a [StudyProtocol].
 */
data class ProtocolOwner( val id: UUID = UUID.randomUUID() )