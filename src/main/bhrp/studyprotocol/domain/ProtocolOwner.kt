package bhrp.studyprotocol.domain

import java.util.*


/**
 * Uniquely identifies the person or group that created a [StudyProtocol].
 */
class ProtocolOwner
{
    val id: UUID = UUID.randomUUID()
}