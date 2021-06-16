package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.domain.StudyDeployment
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
     * The participants that are part of this deployment.
     */
    val participants: Set<Participant>
)
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment.
     */
    val id: UUID get() = studyDeploymentStatus.studyDeploymentId
}
