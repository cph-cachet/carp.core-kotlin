package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot


/**
 * Throw [IllegalArgumentException] when [invitations] don't match the requirements of the protocol.
 *
 * @throws IllegalArgumentException when:
 *  - [invitations] is empty
 *  - any of the assigned device roles in [invitations] is not part of the study protocol
 *  - not all necessary master devices part of the study protocol have been assigned a participant
 */
fun StudyProtocolSnapshot.throwIfInvalidInvitations( invitations: List<ParticipantInvitation> )
{
    require( invitations.isNotEmpty() ) { "No participants invited." }

    val assignedMasterDeviceRoleNames = invitations.flatMap { it.assignedMasterDeviceRoleNames }.toSet()
    assignedMasterDeviceRoleNames.forEach { assigned ->
        require( assigned in masterDevices.map { it.roleName } )
            { "The assigned master device with role name \"$assigned\" is not part of the study protocol." }
    }
    val requiredMasterDeviceRoleNames = masterDevices.filter { !it.isOptional }.map { it.roleName }
    require( assignedMasterDeviceRoleNames.containsAll( requiredMasterDeviceRoleNames ) )
        { "Not all necessary devices required for this study have been assigned to a participant." }
}

/**
 * Throw [IllegalArgumentException] when [connectedDevicePreregistrations] don't match the requirements of the protocol.
 *
 * @throws IllegalArgumentException when:
 *  - one of the role names in [connectedDevicePreregistrations] isn't defined in the study protocol
 *  - an invalid registration for one of the devices is passed
 */
fun StudyProtocolSnapshot.throwIfInvalidPreregistrations(
    connectedDevicePreregistrations: Map<String, DeviceRegistration>
)
{
    connectedDevicePreregistrations.forEach { (roleName, registration) ->
        val connectedDevice = connectedDevices.firstOrNull { it.roleName == roleName }
        requireNotNull( connectedDevice )
            { "The device with role name \"$roleName\" for which a preregistration was defined isn't a connected device in the study protocol." }
        val isInvalidRegistration = connectedDevice.isDefinitelyInvalidRegistration( registration )
        require( !isInvalidRegistration )
            { "The preregistration for the connected device with role name \"$roleName\" is invalid." }
    }
}
