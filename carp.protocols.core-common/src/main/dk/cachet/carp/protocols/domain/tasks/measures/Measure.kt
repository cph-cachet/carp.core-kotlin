package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.common.Immutable
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * Defines data that needs to be measured/collected as part of a task defined by [TaskDescriptor].
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