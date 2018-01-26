package carp.protocols.domain.data

import carp.protocols.domain.common.Immutable
import carp.protocols.domain.notImmutableErrorFor


/**
 * Defines a type of data which can be measured/collected.
 */
abstract class DataType : Immutable( notImmutableErrorFor( DataType::class ) )