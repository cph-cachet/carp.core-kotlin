package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.Serializable


/**
 * A group of one or more [Participant]s participating in a [StudyDeployment].
 */
@Serializable
data class ParticipantGroupStatus(
    /**
     * The deployment status associated with this participant group.
     */
    val studyDeploymentStatus: StudyDeploymentStatus,
    /**
     * The participants and assigned anonymized participation IDs that are part of this deployment.
     */
    val participants: Set<DeanonymizedParticipation>,
    /**
     * Configurable data related to the participants in this participant group.
     * Data which is not set equals null.
     */
    val data: Map<InputDataType, Data?>
)
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment.
     */
    val id: UUID get() = studyDeploymentStatus.studyDeploymentId
}
