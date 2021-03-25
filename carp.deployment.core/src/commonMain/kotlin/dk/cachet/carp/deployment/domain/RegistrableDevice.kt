package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Contains information about devices which can be registered in a deployment.
 */
@Serializable
data class RegistrableDevice(
    /**
     * The description of the device.
     */
    val device: AnyDeviceDescriptor,
    /**
     * Determines whether this device requires deployment after it has been registered.
     */
    val requiresDeployment: Boolean
)
