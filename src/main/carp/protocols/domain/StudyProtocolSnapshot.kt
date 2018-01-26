package carp.protocols.domain

import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
data class StudyProtocolSnapshot( val ownerId: String, val name: String )
{
    companion object Factory {
        fun fromProtocol( protocol: StudyProtocol ): StudyProtocolSnapshot
        {
            return StudyProtocolSnapshot( protocol.owner.id.toString(), protocol.name )
        }
    }
}