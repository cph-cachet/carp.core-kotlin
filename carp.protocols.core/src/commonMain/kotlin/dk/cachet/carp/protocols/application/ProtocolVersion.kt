package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Specifies a specific version for a [StudyProtocol], identified by a [tag].
 *
 * @param date The date when this version of the protocol was created.
 */
@Serializable
data class ProtocolVersion( val tag: String, @Required val date: DateTime = DateTime.now() )
