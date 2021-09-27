package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.domain.StudyDeployment
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * A group of one or more [Participant]s participating in a [StudyDeployment].
 *
 * TODO: This should become a state machine, reflecting the distinction between staging and invited.
 */
@Serializable
data class ParticipantGroupStatus(
    /**
     * The deployment status associated with this participant group.
     */
    val studyDeploymentStatus: StudyDeploymentStatus,
    /**
     * The time at which the participant group was invited.
     */
    val invitedOn: Instant,
    /**
     * The participants that are part of this group.
     */
    val participants: Set<Participant>
)
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment.
     */
    val id: UUID get() = studyDeploymentStatus.studyDeploymentId
}
