package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationSerializer
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptorSerializer
import kotlinx.serialization.Serializable


/**
 * Master [device] and its current [registration] assigned to participants as part of a [ParticipantGroup].
 */
@Serializable
data class AssignedMasterDevice(
    @Serializable( MasterDeviceDescriptorSerializer::class )
    val device: AnyMasterDeviceDescriptor,
    @Serializable( DeviceRegistrationSerializer::class )
    val registration: DeviceRegistration?
)
