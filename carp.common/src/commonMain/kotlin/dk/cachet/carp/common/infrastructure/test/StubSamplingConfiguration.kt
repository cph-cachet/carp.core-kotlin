package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.Serializable


@Serializable
data class StubSamplingConfiguration( val configuration: String ) : SamplingConfiguration
