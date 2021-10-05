package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.infrastructure.serialization.ApplicationDataSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * Contains the entire description and configuration for how a single master device participates in running a study.
 */
@Serializable
data class MasterDeviceDeployment(
    /**
     * The descriptor for the master device this deployment is intended for.
     */
    val deviceDescriptor: AnyMasterDeviceDescriptor,
    /**
     * Configuration for this master device.
     */
    val configuration: DeviceRegistration,
    /**
     * The devices this device needs to connect to.
     */
    val connectedDevices: Set<AnyDeviceDescriptor> = emptySet(),
    /**
     * Preregistration of connected devices, including configuration such as connection properties, stored per role name.
     */
    val connectedDeviceConfigurations: Map<String, DeviceRegistration> = emptyMap(),
    /**
     * All tasks which should be able to be executed on this or connected devices.
     */
    val tasks: Set<TaskDescriptor> = emptySet(),
    /**
     * All triggers originating from this device and connected devices, stored per assigned id unique within the study protocol.
     */
    val triggers: Map<Int, Trigger<*>> = emptyMap(),
    /**
     * Determines which tasks need to be started or stopped when the conditions defined by [triggers] are met.
     */
    val taskControls: Set<TaskControl> = emptySet(),
    /**
     * Application-specific data to be stored as part of a study deployment.
     *
     * This can be used by infrastructures or concrete applications which require exchanging additional data
     * between the protocols and clients subsystems, outside of scope or not yet supported by CARP core.
     */
    @Serializable( ApplicationDataSerializer::class )
    val applicationData: String? = null
)
{
    /**
     * Runtime info of a master device or connected device (determined by [isConnectedDevice]) in a study deployment.
     */
    data class RuntimeDeviceInfo(
        val descriptor: AnyDeviceDescriptor,
        val isConnectedDevice: Boolean,
        /**
         * The matching device [registration] for device [descriptor] in case it has been registered; null otherwise.
         */
        val registration: DeviceRegistration?,
        /**
         * The sampling configuration per data type to use for device when no custom sampling configuration is provided
         * by an ongoing measure.
         */
        val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>
    )

    /**
     * The set of [tasks] which may need to be executed on a master [device], or a connected [device], during a deployment.
     */
    data class DeviceTasks( val device: RuntimeDeviceInfo, val tasks: Set<TaskDescriptor> )


    /**
     * The time when this device deployment was last updated.
     * This corresponds to the most recent device registration as part of this device deployment.
     */
    val lastUpdatedOn: Instant =
        // TODO: Remove this workaround once JS serialization bug is fixed:
        //  https://github.com/Kotlin/kotlinx.serialization/issues/716
        @Suppress( "SENSELESS_COMPARISON" )
        if ( connectedDeviceConfigurations == null || configuration == null ) Clock.System.now()
        else connectedDeviceConfigurations.values.plus( configuration )
            .maxOf { it.registrationCreatedOn }


    /**
     * Get info on the master device and each of the devices this device needs to connect to relevant at study runtime.
     */
    fun getRuntimeDeviceInfo(): List<RuntimeDeviceInfo> =
        connectedDevices.map {
            RuntimeDeviceInfo(
                it,
                isConnectedDevice = true,
                connectedDeviceConfigurations[ it.roleName ],
                getDefaultSamplingConfigurations( it )
            )
        }
        .plus(
            // Master device runtime info.
            RuntimeDeviceInfo(
                deviceDescriptor,
                isConnectedDevice = false,
                configuration,
                getDefaultSamplingConfigurations( deviceDescriptor )
            )
        )

    private fun getDefaultSamplingConfigurations( device: AnyDeviceDescriptor ): Map<DataType, SamplingConfiguration> =
        device.getDataTypeSamplingSchemes().map { (dataType, samplingScheme) ->
            val configuration = device.defaultSamplingConfiguration[ dataType ] ?: samplingScheme.default
            dataType to configuration
        }
        .toMap()

    /**
     * Retrieves for this master device and all connected devices
     * the set of tasks which may be sent to them over the course of the deployment, or an empty set in case there are none.
     * Tasks which target other master devices are not included in this collection.
     */
    fun getTasksPerDevice(): List<DeviceTasks> = getRuntimeDeviceInfo()
        .map { device ->
            val tasks = taskControls
                .filter { it.destinationDeviceRoleName == device.descriptor.roleName }
                .map { triggered -> tasks.first { it.name == triggered.taskName } }
            DeviceTasks( device, tasks.toSet() )
        }
}
