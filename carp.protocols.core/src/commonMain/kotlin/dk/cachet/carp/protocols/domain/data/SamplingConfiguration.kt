package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * Contains configuration on how to sample a data stream of a given type.
 */
@Serializable
@Polymorphic
abstract class SamplingConfiguration : Immutable( notImmutableErrorFor( SamplingConfiguration::class ) )

