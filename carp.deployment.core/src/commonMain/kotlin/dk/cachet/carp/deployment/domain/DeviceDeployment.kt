package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.HashSetSerializer


/**
 * Custom serializer for a set of [DeviceDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [DeviceDescriptor].
 */
object DevicesSetSerializer : KSerializer<Set<DeviceDescriptor<*>>> by HashSetSerializer( DeviceDescriptorSerializer )

/**
 * Custom serializer for a set of [TaskDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [TaskDescriptor].
 */
object TasksSetSerializer : KSerializer<Set<TaskDescriptor>> by HashSetSerializer<TaskDescriptor>(
    createUnknownPolymorphicSerializer { className, json, serializer -> CustomTaskDescriptor( className, json, serializer ) }
)

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
    @Serializable( DevicesSetSerializer::class )
    val connectedDevices: Set<DeviceDescriptor<*>>,
    /**
     * Preregistration of connected devices, including configuration such as connection properties, stored per role name.
     */
    @Serializable( RegisteredDevicesSerializer::class )
    val connectedDeviceConfigurations: Map<String, DeviceRegistration>,
    /**
     * All tasks which should be able to be executed on this or connected devices.
     */
    @Serializable( TasksSetSerializer::class )
    val tasks: Set<TaskDescriptor> )