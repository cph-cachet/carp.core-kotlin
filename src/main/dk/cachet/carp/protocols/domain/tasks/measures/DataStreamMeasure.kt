package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Defines data that needs to be measured/collected from a data stream on a [DeviceDescriptor].
 */
@Serializable
abstract class DataStreamMeasure : Measure()