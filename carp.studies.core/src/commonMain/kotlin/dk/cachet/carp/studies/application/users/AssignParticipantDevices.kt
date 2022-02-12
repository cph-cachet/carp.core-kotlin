package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable


/**
 * Assign primary devices, identified by the [primaryDeviceRoleNames] of a study protocol, to the participant identified by [participantId].
 */
@Serializable
data class AssignParticipantDevices( val participantId: UUID, val primaryDeviceRoleNames: Set<String> )

/**
 * Get the unique set of participant IDs defined in a collection of [AssignParticipantDevices].
 */
fun Collection<AssignParticipantDevices>.participantIds(): Set<UUID> = this.map { it.participantId }.toSet()

/**
 * Get the unique set of primary device roles defined in a collection of [AssignParticipantDevices].
 */
fun Collection<AssignParticipantDevices>.deviceRoles(): Set<String> = this.flatMap { it.primaryDeviceRoleNames }.toSet()
