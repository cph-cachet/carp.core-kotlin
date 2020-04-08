package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorSerializer
import kotlinx.serialization.Serializable


/**
 * Describes the status of a device, part of a study deployment.
 */
@Serializable
sealed class DeviceDeploymentStatus
{
    /**
     * The description of the device.
     */
    @Serializable( DeviceDescriptorSerializer::class )
    abstract val device: AnyDeviceDescriptor
    /**
     * Determines whether the device requires a device deployment by retrieving [MasterDeviceDeployment].
     * Not all master devices necessarily need deployment; chained master devices do not.
     */
    abstract val requiresDeployment: Boolean


    /**
     * A device deployment status which indicates the correct deployment has not been deployed yet.
     */
    interface NotDeployed
    {
        /**
         * Determines whether the device has been registered successfully and is ready for deployment.
         */
        val isReadyForDeployment: Boolean
    }


    /**
     * Device deployment status for when a device has not been registered.
     */
    @Serializable
    data class Unregistered(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val requiresDeployment: Boolean
    ) : DeviceDeploymentStatus(), NotDeployed
    {
        // Devices always need to be registered before they can be deployed.
        override val isReadyForDeployment = false
    }

    /**
     * Device deployment status for when a device has been registered.
     */
    @Serializable
    data class Registered(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val requiresDeployment: Boolean,
        override val isReadyForDeployment: Boolean
    ) : DeviceDeploymentStatus(), NotDeployed

    /**
     * Device deployment status when the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    @Serializable
    data class Deployed(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor
    ) : DeviceDeploymentStatus()
    {
        // All devices that can be deployed need to be deployed.
        override val requiresDeployment = true
    }

    /**
     * Device deployment status when the device has previously been deployed correctly, but due to changes in device registrations needs to be redeployed.
     */
    @Serializable
    data class NeedsRedeployment(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val isReadyForDeployment: Boolean
    ) : DeviceDeploymentStatus(), NotDeployed
    {
        // Only devices that can be deployed ever need to be redeployed, and all those require deployment.
        override val requiresDeployment = true
    }
}
