package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import dk.cachet.carp.common.infrastructure.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration


/**
 * Sampling scheme which allows configuring a time interval in between subsequent measurements.
 */
class IntervalSamplingScheme( dataType: DataTypeMetaData, val defaultMeasureInterval: Duration ) :
    DataTypeSamplingScheme<IntervalSamplingConfigurationBuilder>(
        dataType,
        IntervalSamplingConfiguration( defaultMeasureInterval )
    )
{
    override fun createSamplingConfigurationBuilder(): IntervalSamplingConfigurationBuilder =
        IntervalSamplingConfigurationBuilder( defaultMeasureInterval )

    override fun isValid( configuration: SamplingConfiguration ) = configuration is IntervalSamplingConfiguration
}


/**
 * A sampling configuration which allows configuring the time [interval] in between subsequent measurements.
 */
@Serializable
data class IntervalSamplingConfiguration(
    @Serializable( DurationSerializer::class )
    val interval: Duration
) : SamplingConfiguration


/**
 * A helper class to configure and construct immutable [IntervalSamplingConfiguration] objects
 * as part of setting up a [DeviceDescriptor].
 */
class IntervalSamplingConfigurationBuilder( var interval: Duration ) :
    SamplingConfigurationBuilder<IntervalSamplingConfiguration>
{
    override fun build(): IntervalSamplingConfiguration = IntervalSamplingConfiguration( interval )
}
