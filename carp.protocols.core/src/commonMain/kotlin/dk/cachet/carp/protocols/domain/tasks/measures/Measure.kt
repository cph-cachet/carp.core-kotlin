package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.ImplementAsDataClass
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import kotlinx.serialization.Polymorphic


/**
 * Defines data that needs to be measured/collected from a data stream on a [DeviceDescriptor],
 * as part of a task defined by [TaskDescriptor].
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface Measure
{
    /**
     * The type of data this measure collects.
     */
    val type: DataType
}
