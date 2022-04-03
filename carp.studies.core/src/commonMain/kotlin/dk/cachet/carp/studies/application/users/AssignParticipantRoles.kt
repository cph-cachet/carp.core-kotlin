package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import kotlinx.serialization.Serializable


/**
 * Assign participant roles specified in a study protocol ([assignedRoles])
 * to the participant identified by [participantId].
 */
@Serializable
data class AssignParticipantRoles( val participantId: UUID, val assignedRoles: AssignedTo )

/**
 * Get the unique set of participant IDs defined in a collection of [AssignParticipantRoles].
 */
fun Collection<AssignParticipantRoles>.participantIds(): Set<UUID> = this.map { it.participantId }.toSet()

/**
 * Get the unique set of participant roles defined in a collection of [AssignParticipantRoles].
 */
fun Collection<AssignParticipantRoles>.participantRoles(): Set<String> = this
    .flatMap {
        when ( it.assignedRoles )
        {
            is AssignedTo.Anyone -> emptySet()
            is AssignedTo.Roles -> it.assignedRoles.roleNames
        }
    }
    .toSet()
