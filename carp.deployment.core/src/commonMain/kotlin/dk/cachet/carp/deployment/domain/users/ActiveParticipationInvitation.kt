package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import kotlinx.serialization.Serializable


/**
 * An [invitation] to participate in an active study deployment using the specified master [devices].
 * Some of the devices which the participant is invited to might already be registered.
 * If the participant wants to use a different device, they will need to unregister the existing device first.
 */
@Serializable
data class ActiveParticipationInvitation(
    val participation: Participation,
    val invitation: StudyInvitation,
    val devices: Set<DeviceInvitation>
)
{
    @Serializable
    data class DeviceInvitation(
        val deviceRoleName: String,
        /**
         * True when the device is already registered in the study deployment; false otherwise.
         * In case a device is registered, it needs to be unregistered first before a new device can be registered.
         */
        val isRegistered: Boolean
    )
}


/**
 * Filter the given [invitations] to only return those that have active study deployments.
 * This subset is returned as [ActiveParticipationInvitation]s,
 * appending the current device registration status to the devices the participant was invited to use.
 *
 * @throws IllegalArgumentException when a deployment for one of the [invitations] is missing in [deployments],
 * or when the device roles specified in [invitations] do not match the deployment.
 */
internal fun filterActiveParticipationInvitations(
    invitations: Set<ParticipationInvitation>,
    deployments: List<StudyDeployment>
): Set<ActiveParticipationInvitation>
{
    return invitations
        .map { it to (
            deployments.firstOrNull { d -> d.id == it.participation.studyDeploymentId }
                ?.getStatus()
                ?: throw IllegalArgumentException( "No deployment is passed to pair with one of the given invitations." )
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
