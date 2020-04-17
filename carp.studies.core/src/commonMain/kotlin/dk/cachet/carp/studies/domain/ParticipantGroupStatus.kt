package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class ParticipantGroupStatus(
    /**
     * The deployment status associated with this participant group.
     */
    val studyDeploymentStatus: StudyDeploymentStatus,
    /**
     * The participants and assigned anonymized participation IDs that are part of this deployment.
     * TODO: This redundantly stores `studyDeploymentId` inside of the Participation.
     *       Rather than adding another class, e.g., `ParticipantGroupMember`,
     *       I think we should simply remove the ID and store participations per `studyDeploymentId` in `Study`.
     */
    val participants: Set<DeanonymizedParticipation>
)
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment.
     */
    @Transient
    val id: UUID get() = studyDeploymentStatus.studyDeploymentId
}
