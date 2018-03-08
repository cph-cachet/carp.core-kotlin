package dk.cachet.carp.protocols.domain

import java.time.LocalDateTime


/**
 * Specifies a specific version for a [StudyProtocol].
 *
 * @param date The date when this version of the protocol was created.
 * @param tag A descriptive tag which uniquely identifies this protocol version.
 */
data class ProtocolVersion( val date: LocalDateTime, val tag: String )