package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorSerializer
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
     * Determines whether the device can be deployed by retrieving [MasterDeviceDeployment].
     * Not all master devices necessarily need deployment; chained master devices do not.
     */
    val canBeDeployed: Boolean,
    /**
     * Determines whether this device can be deployed and requires deployment in order to start the study.
     */
    val requiresDeployment: Boolean
)
