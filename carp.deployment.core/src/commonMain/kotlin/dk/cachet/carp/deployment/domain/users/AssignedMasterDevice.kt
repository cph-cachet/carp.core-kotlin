package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlinx.serialization.Serializable


/**
 * Master [device] and its current [registration] assigned to participants as part of a [ParticipantGroup].
 */
@Serializable
data class AssignedMasterDevice( val device: AnyMasterDeviceDescriptor, val registration: DeviceRegistration? )
