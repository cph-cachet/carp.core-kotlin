package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.Participation
import kotlinx.serialization.Serializable


/**
 * Links a [Participant] (and its associated meta-data) to an anonymized [Participation] in a study deployment.
 */
@Serializable
data class DeanonymizedParticipation( val participantId: UUID, val participationId: UUID )
