package dk.cachet.carp.protocols.domain.sampling

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * A sampling configuration which does not provide any configuration options.
 */
@Serializable
data class NoOptionsSamplingConfiguration(
    /**
     * HACK: This is only here because the Immutable base class of SamplingConfiguration currently does not allow this to be an object.
     *       Remove once fixed: https://github.com/cph-cachet/carp.core-kotlin/issues/121
     */
    @Transient
    private val ignored: String = ""
) : SamplingConfiguration


/**
 * A [SamplingConfiguration] builder for [DataTypeSamplingScheme]s which cannot be configured.
 */
object NoOptionsSamplingConfigurationBuilder : SamplingConfigurationBuilder
{
    override fun build(): NoOptionsSamplingConfiguration = NoOptionsSamplingConfiguration()
}
