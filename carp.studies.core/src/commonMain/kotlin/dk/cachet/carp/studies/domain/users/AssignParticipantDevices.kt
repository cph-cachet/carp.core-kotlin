package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Assign devices, identified by the [deviceRoleNames] of a study protocol, to the participant identified by [participantId].
 */
@Serializable
data class AssignParticipantDevices( val participantId: UUID, val deviceRoleNames: Set<String> )

/**
 * Get the unique set of participant IDs defined in a collection of [AssignParticipantDevices].
 */
fun Collection<AssignParticipantDevices>.participantIds(): Set<UUID> = this.map { it.participantId }.toSet()

/**
 * Get the unique set of device roles defined in a collection of [AssignParticipantDevices].
 */
fun Collection<AssignParticipantDevices>.deviceRoles(): Set<String> = this.flatMap { it.deviceRoleNames }.toSet()
