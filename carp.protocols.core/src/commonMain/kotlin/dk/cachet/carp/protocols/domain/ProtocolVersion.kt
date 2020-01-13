package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.DateTime


/**
 * Specifies a specific version for a [StudyProtocol].
 *
 * @param date The date when this version of the protocol was created.
 * @param tag A descriptive tag which uniquely identifies this protocol version.
 */
data class ProtocolVersion( val date: DateTime, val tag: String )
