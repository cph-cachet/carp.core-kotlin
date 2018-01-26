package carp.protocols.domain.tasks

import carp.protocols.domain.common.Immutable
import carp.protocols.domain.data.DataType
import carp.protocols.domain.notImmutableErrorFor


/**
 * Defines data that needs to be measured/collected as part of a task defined by [TaskDescriptor].
 */
data class Measure( val type: DataType ) : Immutable( notImmutableErrorFor( Measure::class ) )