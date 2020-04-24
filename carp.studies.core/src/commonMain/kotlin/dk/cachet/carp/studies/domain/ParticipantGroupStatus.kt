package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


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
    val participants: Set<DeanonymizedParticipation>
)
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment.
     */
    @Transient
    val id: UUID get() = studyDeploymentStatus.studyDeploymentId
}
