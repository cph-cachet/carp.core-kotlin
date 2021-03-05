package dk.cachet.carp.protocols.domain.sampling

import kotlinx.serialization.Serializable


/**
 * A sampling configuration which does not provide any configuration options.
 */
@Serializable
object NoOptionsSamplingConfiguration : SamplingConfiguration


/**
 * A [SamplingConfiguration] builder for [DataTypeSamplingScheme]s which cannot be configured.
 */
object NoOptionsSamplingConfigurationBuilder : SamplingConfigurationBuilder
{
    override fun build(): NoOptionsSamplingConfiguration = NoOptionsSamplingConfiguration
}
