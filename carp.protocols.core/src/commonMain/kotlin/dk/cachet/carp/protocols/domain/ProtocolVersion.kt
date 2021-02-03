package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.DateTime
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Specifies a specific version for a [StudyProtocol], identified by a [tag].
 *
 * @param date The date when this version of the protocol was created.
 */
@Serializable
data class ProtocolVersion( val tag: String, @Required val date: DateTime = DateTime.now() )
