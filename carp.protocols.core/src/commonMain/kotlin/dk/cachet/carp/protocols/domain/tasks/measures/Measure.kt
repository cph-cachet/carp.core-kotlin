package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * Defines data that needs to be measured/collected from a data stream on a [DeviceDescriptor],
 * as part of a task defined by [TaskDescriptor].
 */
@Serializable
@Polymorphic
abstract class Measure : Immutable( notImmutableErrorFor( Measure::class ) )
{
    /**
     * The type of data this measure collects.
     */
    abstract val type: DataType
}
