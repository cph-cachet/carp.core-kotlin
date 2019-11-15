package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies the person or group that created a [StudyProtocol].
 */
@Serializable
data class ProtocolOwner(
    @Serializable( with = UUIDSerializer::class )
    val id: UUID = UUID.randomUUID() )