package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import kotlinx.serialization.Serializable


/**
 * A participant, uniquely identified by [participation], linked to an account identified by [accountId],
 * which was invited to a study deployment (using [invitation]) using the devices with [assignedPrimaryDeviceRoleNames].
 */
@Serializable
data class AccountParticipation(
    val participation: Participation,
    val assignedPrimaryDeviceRoleNames: Set<String>,
    val accountId: UUID,
    val invitation: StudyInvitation
)


/**
 * Filter the given [invitations] to only return those that have active study deployments.
 * This subset is returned as [ActiveParticipationInvitation]s,
 * appending the current device registration status to the devices the participant was invited to use.
 *
 * @throws IllegalArgumentException when a participant group for one of the [invitations] is missing in [groups],
 * or when the device roles specified in [invitations] do not match the assigned devices.
 */
internal fun filterActiveParticipationInvitations(
    invitations: Set<AccountParticipation>,
    groups: List<ParticipantGroup>
): Set<ActiveParticipationInvitation> = invitations
    .map {
        val matchingGroup = groups.firstOrNull { g -> g.studyDeploymentId == it.participation.studyDeploymentId }
        requireNotNull( matchingGroup ) { "No deployment is passed to pair with one of the given invitations." }
        it to matchingGroup
    }
    .filter { (_, group) -> !group.isStudyDeploymentStopped }
    .map { (invitation, group) ->
        ActiveParticipationInvitation(
            invitation.participation,
            invitation.invitation,
            invitation.assignedPrimaryDeviceRoleNames.map { group.getAssignedPrimaryDevice( it ) }.toSet()
        )
    }.toSet()
