package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable


/**
 * Links an [externalId] (e.g., a participant ID and its associated meta-data)
 * to an anonymized [participationId] in a study deployment.
 */
@Serializable
data class DeanonymizedParticipation( val externalId: UUID, val participationId: UUID )

