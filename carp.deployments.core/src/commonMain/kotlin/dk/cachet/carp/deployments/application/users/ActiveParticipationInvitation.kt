package dk.cachet.carp.deployments.application.users

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
    val assignedDevices: Set<AssignedPrimaryDevice>
)
