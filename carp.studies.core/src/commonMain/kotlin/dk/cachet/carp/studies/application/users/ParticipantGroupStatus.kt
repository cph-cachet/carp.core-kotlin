package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.domain.StudyDeployment
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * A group of one or more [participants] which is first [Staged] to later be [Invited] to a [StudyDeployment].
 */
@Serializable
sealed class ParticipantGroupStatus
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment once deployed.
     */
    abstract val id: UUID

    /**
     * The participants that are part of this group.
     */
    abstract val participants: Set<Participant>


    /**
     * The [participants] have not yet been invited. The list of participants can still be modified.
     */
    @Serializable
    data class Staged(
        override val id: UUID,
        override val participants: Set<Participant>
    ) : ParticipantGroupStatus()

    /**
     * The [participants] have been invited to a study deployment.
     */
    @Serializable
    data class Invited(
        override val id: UUID,
        override val participants: Set<Participant>,
        /**
         * The time at which the participant group was invited.
         */
        val invitedOn: Instant,
        /**
         * The deployment status of the study deployment the participants were invited to.
         */
        val studyDeploymentStatus: StudyDeploymentStatus
    ) : ParticipantGroupStatus()
}
