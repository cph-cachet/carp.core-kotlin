package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies the participation of an account in a study deployment.
 */
@Serializable
data class Participation(
    val studyDeploymentId: UUID,
    /**
     * The invitation to participate in this study which should be sent to the participant.
     */
    val invitation: StudyInvitation,
    val id: UUID = UUID.randomUUID()
)
