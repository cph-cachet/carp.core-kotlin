package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.Participation
import kotlinx.serialization.Serializable


/**
 * Links a [Participant] (and its associated meta-data) to an anonymized [Participation] in a study deployment.
 */
@Serializable
data class DeanonymizedParticipation(
    val participantId: UUID,
    val participationId: UUID,
    val assignedDeviceRoleNames: Set<String>
)
