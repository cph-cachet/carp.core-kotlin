package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import kotlinx.serialization.Serializable


@Serializable
data class StubSamplingConfiguration( val configuration: String ) : SamplingConfiguration()
