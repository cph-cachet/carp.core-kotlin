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
     * Determines whether the device requires a device deployment, and if so,
     * whether the deployment configuration (to initialize the device environment) can be obtained.
     * This requires the specified device and all other master devices it depends on to be registered.
     */
    val canObtainDeviceDeployment: Boolean
        get() = this is Deployed || (this is NotDeployed && this.remainingDevicesToRegisterToObtainDeployment.isEmpty())


    /**
     * A device deployment status which indicates the correct deployment has not been deployed yet.
     */
    interface NotDeployed
    {
        val requiresDeployment: Boolean

        /**
         * The role names of devices which need to be registered before the deployment information for this device can be obtained.
         */
        val remainingDevicesToRegisterToObtainDeployment: Set<String>

        /**
         * The role names of devices which need to be registered before this device can be declared as successfully deployed.
         */
        val remainingDevicesToRegisterBeforeDeployment: Set<String>
    }


    /**
     * Device deployment status for when a device has not been registered.
     */
    @Serializable
    data class Unregistered(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val requiresDeployment: Boolean,
        override val remainingDevicesToRegisterToObtainDeployment: Set<String>,
        override val remainingDevicesToRegisterBeforeDeployment: Set<String>
    ) : DeviceDeploymentStatus(), NotDeployed

    /**
     * Device deployment status for when a device has been registered.
     */
    @Serializable
    data class Registered(
        @Serializable( DeviceDescriptorSerializer::class )
        override val device: AnyDeviceDescriptor,
        override val requiresDeployment: Boolean,
        override val remainingDevicesToRegisterToObtainDeployment: Set<String>,
        override val remainingDevicesToRegisterBeforeDeployment: Set<String>
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
        override val remainingDevicesToRegisterToObtainDeployment: Set<String>,
        override val remainingDevicesToRegisterBeforeDeployment: Set<String>
    ) : DeviceDeploymentStatus(), NotDeployed
    {
        // Only devices that can be deployed ever need to be redeployed, and all those require deployment.
        override val requiresDeployment = true
    }
}
