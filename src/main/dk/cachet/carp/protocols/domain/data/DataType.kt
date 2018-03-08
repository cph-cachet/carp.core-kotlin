package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.common.Immutable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.Serializable


/**
 * Defines a type of data which can be measured/collected.
 */
@Serializable
abstract class DataType : Immutable( notImmutableErrorFor( DataType::class ) )