package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus


object ParticipationService
{
    /**
     * Filter the given [invitations] to only return those that have active study deployments.
     * This subset is returned as [ActiveParticipationInvitation]s,
     * appending the current device registration status to the devices the participant was invited to use.
     */
    fun filterActiveParticipationInvitations(
        invitations: Set<ParticipationInvitation>,
        deployments: List<StudyDeployment>
    ): Set<ActiveParticipationInvitation>
    {
        return invitations
            .map { it to (
                deployments.firstOrNull { d -> d.id == it.participation.studyDeploymentId }
                    ?.getStatus()
                    ?: error( "No deployment is passed to pair with one of the given invitations." )
            ) }
            .filter { it.second !is StudyDeploymentStatus.Stopped }
            .map {
                ActiveParticipationInvitation(
                    it.first.participation,
                    it.first.invitation,
                    it.first.deviceRoleNames.map { deviceRoleName ->
                        ActiveParticipationInvitation.DeviceInvitation(
                            deviceRoleName,
                            it.second.getDeviceStatus( deviceRoleName ) is DeviceDeploymentStatus.Registered
                        )
                    }.toSet()
                )
            }.toSet()
    }
}
