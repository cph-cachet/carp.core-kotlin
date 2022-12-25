package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.sampling.SamplingConfigurationBuilder
import kotlinx.serialization.*


@Serializable
data class StubSamplingConfiguration( val configuration: String ) : SamplingConfiguration


class StubSamplingConfigurationBuilder( var configuration: String ) :
    SamplingConfigurationBuilder<StubSamplingConfiguration>
{
    override fun build(): StubSamplingConfiguration = StubSamplingConfiguration( configuration )
}
