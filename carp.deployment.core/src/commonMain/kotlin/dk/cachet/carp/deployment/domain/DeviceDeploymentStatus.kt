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
     * Determines whether registering the device is required for the deployment to start running.
     */
    abstract val requiresRegistration: Boolean
    /**
     * Determines whether the device requires a device deployment by retrieving [MasterDeviceDeployment].
     * Not all master devices necessarily need deployment; chained master devices do not.
     */
    abstract val requiresDeployment: Boolean


    /**
     * A device deployment status which has not been deployed yet.
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
        override val requiresRegistration: Boolean,
        override val requiresDeployment: Boolean
    ) : DeviceDeploymentStatus(), NotDeployed
    {
        override val isReadyForDeployment = false
    }

    /**
     * Device deployment status for when a device has been registered.
     */
    @Serializable
    data class Registered(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val requiresRegistration: Boolean,
        override val requiresDeployment: Boolean,
        override val isReadyForDeployment: Boolean
    ) : DeviceDeploymentStatus(), NotDeployed

    /**
     * Device deployment status when the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    @Serializable
    data class Deployed(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val requiresRegistration: Boolean,
        override val requiresDeployment: Boolean
    ) : DeviceDeploymentStatus()
}
