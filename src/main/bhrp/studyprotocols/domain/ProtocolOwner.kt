package bhrp.studyprotocols.domain

import java.util.*


/**
 * Uniquely identifies the person or group that created a [StudyProtocol].
 */
data class ProtocolOwner( val id: UUID = UUID.randomUUID() )