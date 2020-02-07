package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies the participation of an account in a study deployment.
 */
@Serializable
data class Participation(
    val studyDeploymentId: UUID,
    val id: UUID = UUID.randomUUID()
)
