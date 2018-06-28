package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.common.Immutable
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.Serializable


/**
 * Defines data that needs to be measured/collected as part of a task defined by [TaskDescriptor].
 */
@Serializable
abstract class Measure : Immutable( notImmutableErrorFor( Measure::class ) )
{
    /**
     * The type of data this measure collects.
     */
    @Serializable( with = DataTypeSerializer::class )
    abstract val type: DataType
}