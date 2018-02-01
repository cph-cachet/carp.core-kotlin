package carp.protocols.domain.tasks

import carp.protocols.domain.common.Immutable
import carp.protocols.domain.data.DataType
import carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.Serializable


/**
 * Defines data that needs to be measured/collected as part of a task defined by [TaskDescriptor].
 *
 * @param type The type of data this measure collects.
 */
@Serializable
data class Measure( val type: DataType ) : Immutable( notImmutableErrorFor( Measure::class ) )