package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


/**
 * Describes the status of the devices part of a study deployment.
 */
@Serializable
data class DeviceDeploymentStatus(
    /**
     * The description of the device.
     */
    @Serializable( DeviceDescriptorSerializer::class )
    val device: AnyDeviceDescriptor,
    /**
     * Determines whether registering the device is required for the deployment to start running.
     */
    val requiresRegistration: Boolean,
    /**
     * Determines whether the device is currently registered.
     */
    val isRegistered: Boolean )