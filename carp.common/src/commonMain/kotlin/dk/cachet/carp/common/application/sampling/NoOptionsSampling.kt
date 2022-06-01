package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataTypeMetaData
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Sampling scheme which does not allow any sampling configuration.
 */
@JsExport
class NoOptionsSamplingScheme( dataType: DataTypeMetaData ) :
    DataTypeSamplingScheme<NoOptionsSamplingConfigurationBuilder>( dataType, NoOptionsSamplingConfiguration )
{
    override fun createSamplingConfigurationBuilder(): NoOptionsSamplingConfigurationBuilder =
        NoOptionsSamplingConfigurationBuilder

    override fun isValid( configuration: SamplingConfiguration ) = configuration is NoOptionsSamplingConfiguration
}


/**
 * A sampling configuration which does not provide any configuration options.
 */
@Serializable
@JsExport
object NoOptionsSamplingConfiguration : SamplingConfiguration


/**
 * A [SamplingConfiguration] builder for [DataTypeSamplingScheme]s which cannot be configured.
 */
@JsExport
object NoOptionsSamplingConfigurationBuilder : SamplingConfigurationBuilder<NoOptionsSamplingConfiguration>
{
    override fun build(): NoOptionsSamplingConfiguration = NoOptionsSamplingConfiguration
}
