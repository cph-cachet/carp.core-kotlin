package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
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
     * Determines whether this device requires deployment in order to start the study.
     */
    val requiresDeployment: Boolean
)
