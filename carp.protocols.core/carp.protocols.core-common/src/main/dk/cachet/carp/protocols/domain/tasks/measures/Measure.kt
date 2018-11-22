package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import kotlinx.serialization.*


/**
 * Defines data that needs to be measured/collected from a data stream on a [DeviceDescriptor],
 * as part of a task defined by [TaskDescriptor].
 */
@Serializable
abstract class Measure : Immutable( notImmutableErrorFor( Measure::class ) )
{
    /**
     * The type of data this measure collects.
     */
    @Transient
    abstract val type: DataType
}