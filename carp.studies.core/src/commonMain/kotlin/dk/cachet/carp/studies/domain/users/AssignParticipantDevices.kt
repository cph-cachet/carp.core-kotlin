package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Assign master devices, identified by the [masterDeviceRoleNames] of a study protocol, to the participant identified by [participantId].
 */
@Serializable
data class AssignParticipantDevices( val participantId: UUID, val masterDeviceRoleNames: Set<String> )

/**
 * Get the unique set of participant IDs defined in a collection of [AssignParticipantDevices].
 */
fun Collection<AssignParticipantDevices>.participantIds(): Set<UUID> = this.map { it.participantId }.toSet()

/**
 * Get the unique set of master device roles defined in a collection of [AssignParticipantDevices].
 */
fun Collection<AssignParticipantDevices>.deviceRoles(): Set<String> = this.flatMap { it.masterDeviceRoleNames }.toSet()
