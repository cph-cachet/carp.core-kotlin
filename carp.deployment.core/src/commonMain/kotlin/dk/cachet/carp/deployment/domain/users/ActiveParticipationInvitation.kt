package dk.cachet.carp.deployment.domain.users

import kotlinx.serialization.Serializable


/**
 * An [invitation] to participate in an active study deployment using the [assignedDevices].
 * Some of the devices which the participant is invited to might already be registered.
 * If the participant wants to use a different device, they will need to unregister the existing device first.
 */
@Serializable
data class ActiveParticipationInvitation(
    val participation: Participation,
    val invitation: StudyInvitation,
    val assignedDevices: Set<AssignedMasterDevice>
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
): Set<ActiveParticipationInvitation>
{
    return invitations
        .map { it to (
            groups.firstOrNull { g -> g.studyDeploymentId == it.participation.studyDeploymentId }
                ?: throw IllegalArgumentException( "No deployment is passed to pair with one of the given invitations." )
        ) }
        .filter { (_, group) -> !group.isStudyDeploymentStopped }
        .map { (invitation, group) ->
            ActiveParticipationInvitation(
                invitation.participation,
                invitation.invitation,
                invitation.assignedMasterDeviceRoleNames.map { group.getAssignedMasterDevice( it ) }.toSet()
            )
        }.toSet()
}
