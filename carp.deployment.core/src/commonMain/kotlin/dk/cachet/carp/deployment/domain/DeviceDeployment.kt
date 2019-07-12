package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import kotlinx.serialization.Serializable


/**
 * Contains the entire description and configuration for how a single device participates in running a study.
 */
@Serializable
data class DeviceDeployment(
    /**
     * Configuration for this device.
     */
    @Serializable( DeviceRegistrationSerializer::class )
    val configuration: DeviceRegistration,
    /**
     * The devices this device needs to connect to.
     */
    @Serializable( DevicesSerializer::class )
    val connectedDevices: Set<DeviceDescriptor<*>>,
    /**
     * Preregistration of connected devices, including configuration such as connection properties, stored per role name.
     */
    @Serializable( RegisteredDevicesSerializer::class )
    val connectedDeviceConfigurations: Map<String, DeviceRegistration>,
    /**
     * All tasks which should be able to be executed on this or connected devices.
     */
    @Serializable( TasksSerializer::class )
    val tasks: Set<TaskDescriptor> )