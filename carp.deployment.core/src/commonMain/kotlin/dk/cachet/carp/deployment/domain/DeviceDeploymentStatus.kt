package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


/**
 * Describes the status of a device, part of a study deployment.
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
    val isRegistered: Boolean,
    /**
     * Determines whether the device requires a device deployment by retrieving [MasterDeviceDeployment].
     * Not all master devices necessarily need deployment; chained master devices do not.
     */
    val requiresDeployment: Boolean,
    /**
     * Determines whether the device has been registered successfully and is ready for deployment.
     */
    val isReadyForDeployment: Boolean,
    /**
     * True if the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    val isDeployed: Boolean )