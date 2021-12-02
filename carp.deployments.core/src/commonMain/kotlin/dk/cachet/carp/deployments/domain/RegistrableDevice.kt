package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
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
     * Determines whether the device can be deployed by retrieving [MasterDeviceDeployment].
     * Not all master devices necessarily need deployment; chained master devices do not.
     */
    val canBeDeployed: Boolean,
    /**
     * Determines whether this device can be deployed and requires deployment in order to start the study.
     */
    val requiresDeployment: Boolean
)
