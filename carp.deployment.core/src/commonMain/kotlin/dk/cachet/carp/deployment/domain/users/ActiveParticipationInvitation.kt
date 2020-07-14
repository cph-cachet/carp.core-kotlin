package dk.cachet.carp.deployment.domain.users

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
