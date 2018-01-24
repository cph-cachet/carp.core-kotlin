package bhrp.studyprotocols.domain.data

import bhrp.studyprotocols.domain.common.Immutable
import bhrp.studyprotocols.domain.notImmutableErrorFor


/**
 * Defines a type of data which can be measured/collected.
 */
abstract class DataType : Immutable( notImmutableErrorFor( DataType::class ) )