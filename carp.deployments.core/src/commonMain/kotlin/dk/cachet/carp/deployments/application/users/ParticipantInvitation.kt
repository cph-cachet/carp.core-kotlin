package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import kotlinx.serialization.Serializable


/**
 * The information which needs to be provided when inviting a participant to a deployment.
 */
@Serializable
data class ParticipantInvitation(
    /**
     * An ID for the participant, uniquely assigned by the calling service.
     */
    val participantId: UUID,
    /**
     * The participant roles in the study protocol which the participant is assigned to,
     * or [AssignedTo.Anyone] if the participant can choice any/all roles.
     */
    val assignedRoles: AssignedTo,
    /**
     * The identity used to authenticate and invite the participant.
     */
    val identity: AccountIdentity,
    /**
     * A description of the study which is shared with the participant.
     */
    val invitation: StudyInvitation
)
