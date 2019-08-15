package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


/**
 * Contains information about devices which can be registered in a deployment.
 */
@Serializable
data class RegistrableDevice(
    /**
     * The description of the device.
     */
    @Serializable( DeviceDescriptorSerializer::class )
    val device: AnyDeviceDescriptor,
    /**
     * Determines whether registering the device is required for the deployment to start running.
     */
    val requiresRegistration: Boolean )