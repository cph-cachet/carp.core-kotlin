package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import kotlinx.serialization.Serializable


/**
 * Contains information about devices which can be registered in a deployment.
 */
@Serializable
data class RegistrableDevice(
    /**
     * The description of the device.
     */
    val device: AnyDeviceConfiguration,
    /**
     * Determines whether the device can be deployed by retrieving [PrimaryDeviceDeployment].
     * Not all primary devices necessarily need deployment; chained primary devices do not.
     */
    val canBeDeployed: Boolean,
    /**
     * Determines whether this device can be deployed and requires deployment in order to start the study.
     */
    val requiresDeployment: Boolean
)
