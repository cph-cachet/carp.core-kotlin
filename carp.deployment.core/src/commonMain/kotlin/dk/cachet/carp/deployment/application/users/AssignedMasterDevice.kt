package dk.cachet.carp.deployment.application.users

import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import kotlinx.serialization.Serializable


/**
 * Master [device] and its current [registration] assigned to participants as part of a [ParticipantGroup].
 */
@Serializable
data class AssignedMasterDevice( val device: AnyMasterDeviceDescriptor, val registration: DeviceRegistration? )
