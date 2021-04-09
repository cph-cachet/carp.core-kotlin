package dk.cachet.carp.common.application.tasks.measures

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import dk.cachet.carp.common.application.tasks.TaskDescriptor
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
