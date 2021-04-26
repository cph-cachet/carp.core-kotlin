package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.Serializable


/**
 * Defines data that needs to be measured/collected from a data stream on a [DeviceDescriptor],
 * as part of a task defined by [TaskDescriptor].
 */
@Serializable
data class Measure(
    /**
     * The type of data this measure collects.
     */
    val type: DataType,
    /**
     * Override the default configuration on how to sample the data stream of the matching [type] on the device.
     */
    val overrideSamplingConfiguration: SamplingConfiguration? = null
)
