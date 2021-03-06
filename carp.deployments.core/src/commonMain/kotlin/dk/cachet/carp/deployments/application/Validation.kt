package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * Throw [IllegalArgumentException] when [invitations] or [connectedDevicePreregistrations]
 * do not match the requirements of the protocol.
 *
 * @throws IllegalArgumentException when:
 *  - [invitations] is empty
 *  - any of the assigned device roles in [invitations] is not part of the study protocol
 *  - not all master devices part of the study protocol have been assigned a participant
 */
fun StudyProtocolSnapshot.throwIfInvalid(
    invitations: List<ParticipantInvitation>,
    connectedDevicePreregistrations: Map<String, DeviceRegistration> = emptyMap()
)
{
    require( invitations.isNotEmpty() ) { "No participants invited." }

    val masterDevices = this.masterDevices.map { it.roleName }
    val assignedMasterDevices = invitations.flatMap { it.assignedMasterDeviceRoleNames }.toSet()
    assignedMasterDevices.forEach {
        require( it in masterDevices )
            { "The assigned master device with role name \"$it\" is not part of the study protocol." }
    }
    require( assignedMasterDevices.containsAll( masterDevices ) )
        { "Not all devices required for this study have been assigned to a participant." }

    connectedDevicePreregistrations.forEach { (roleName, registration) ->
        val connectedDevice = connectedDevices.firstOrNull { it.roleName == roleName }
        requireNotNull( connectedDevice )
            { "The device with role name \"$roleName\" for which a preregistration was defined isn't a connected device in the study protocol." }
        val isInvalidRegistration = connectedDevice.isDefinitelyInvalidRegistration( registration )
        require( !isInvalidRegistration )
            { "The preregistration for the connected device with role name \"$roleName\" is invalid." }
    }
}
