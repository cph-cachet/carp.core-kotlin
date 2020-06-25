package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorSerializer
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationSerializer
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptorSerializer
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.protocols.domain.triggers.TriggerSerializer
import kotlinx.serialization.Serializable


/**
 * Contains the entire description and configuration for how a single master device participates in running a study.
 */
@Serializable
data class MasterDeviceDeployment(
    /**
     * Configuration for this master device.
     */
    @Serializable( DeviceRegistrationSerializer::class )
    val configuration: DeviceRegistration,
    /**
     * The devices this device needs to connect to.
     */
    val connectedDevices: Set<@Serializable( DeviceDescriptorSerializer::class ) AnyDeviceDescriptor>,
    /**
     * Preregistration of connected devices, including configuration such as connection properties, stored per role name.
     */
    val connectedDeviceConfigurations: Map<String, @Serializable( DeviceRegistrationSerializer::class ) DeviceRegistration>,
    /**
     * All tasks which should be able to be executed on this or connected devices.
     */
    val tasks: Set<@Serializable( TaskDescriptorSerializer::class ) TaskDescriptor>,
    /**
     * All triggers originating from this device and connected devices, stored per assigned id unique within the study protocol.
     */
    val triggers: Map<Int, @Serializable( TriggerSerializer::class ) Trigger>,
    /**
     * The specification of tasks triggered and the devices they are sent to.
     */
    val triggeredTasks: Set<TriggeredTask>
)
{
    /**
     * Specifies the task with [taskName] which is sent to [destinationDeviceRoleName] when the condition of the trigger with [triggerId] is met.
     */
    @Serializable
    data class TriggeredTask(
        /**
         * The id of the [Trigger] which describes the condition which when met sends the task with [taskName] to the device with [destinationDeviceRoleName].
         */
        val triggerId: Int,
        /**
         * The name of the task to send to [destinationDeviceRoleName] when the trigger condition is met.
         */
        val taskName: String,
        /**
         * The role name of the device to which to send the task with [taskName] when the [trigger] condition is met.
         */
        val destinationDeviceRoleName: String
    )


    /**
     * The time when this device deployment was last updated.
     * This corresponds to the most recent device registration as part of this device deployment.
     */
    val lastUpdateDate: DateTime = connectedDeviceConfigurations.values.plus( configuration )
        .map { it.registrationCreationDate.msSinceUTC }
        .max()
        .let { DateTime( it!! ) }
}
