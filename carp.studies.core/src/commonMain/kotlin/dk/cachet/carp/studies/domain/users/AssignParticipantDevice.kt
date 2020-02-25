package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Assign a device, identified by the [deviceRole] of a study protocol, to the participant identified by [participantId].
 */
@Serializable
data class AssignParticipantDevice( val participantId: UUID, val deviceRole: String )

/**
 * Get the unique set of participant IDs defined in a collection of [AssignParticipantDevice].
 */
fun Collection<AssignParticipantDevice>.participantIds(): Set<UUID> = this.map { it.participantId }.toSet()

/**
 * Get the unique set of device roles defined in a collection of [AssignParticipantDevice].
 */
fun Collection<AssignParticipantDevice>.deviceRoles(): Set<String> = this.map { it.deviceRole }.toSet()
