package bhrp.studyprotocols.domain.tasks

import bhrp.studyprotocols.domain.common.Immutable
import bhrp.studyprotocols.domain.data.DataType
import bhrp.studyprotocols.domain.notImmutableErrorFor


/**
 * Defines data that needs to be measured/collected as part of a task defined by [TaskDescriptor].
 */
data class Measure( val type: DataType ) : Immutable( notImmutableErrorFor( Measure::class ) )