package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies a participation of an account in a study deployment.
 */
@Serializable
data class Participation(
    val studyDeploymentId: UUID,
    @Required
    val participantId: UUID = UUID.randomUUID()
)
