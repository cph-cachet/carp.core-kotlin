package dk.cachet.carp.common.application.sampling

import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.infrastructure.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration


/**
 * Sampling scheme which allows configuring a time interval in between subsequent measurements.
 */
class IntervalSamplingScheme(
    dataType: DataTypeMetaData,
    val defaultMeasureInterval: Duration,
    /**
     * A fixed set of [Duration] options which are valid; null if no such restriction exists.
     */
    val validOptions: Set<Duration>? = null
) : DataTypeSamplingScheme<IntervalSamplingConfigurationBuilder>(
    dataType,
    IntervalSamplingConfiguration( defaultMeasureInterval )
)
{
    init
    {
        if ( validOptions != null )
        {
            require( validOptions.isNotEmpty() )
                { "If only a fixed set of options are valid, at least one option needs to be present." }
            require( defaultMeasureInterval in validOptions )
                { "If only a fixed set of options are valid, the default interval needs to be one of the options." }
        }
    }

    override fun createSamplingConfigurationBuilder(): IntervalSamplingConfigurationBuilder =
        IntervalSamplingConfigurationBuilder( defaultMeasureInterval, validOptions )

    override fun isValid( configuration: SamplingConfiguration ) =
        configuration is IntervalSamplingConfiguration &&
        ( validOptions == null || configuration.interval in validOptions )
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
 * as part of setting up a [DeviceConfiguration].
 */
class IntervalSamplingConfigurationBuilder internal constructor(
    var interval: Duration,
    val validOptions: Set<Duration>?
) : SamplingConfigurationBuilder<IntervalSamplingConfiguration>
{
    /**
     * Select the nearest valid option to the requested [interval].
     */
    fun nearestOption( interval: Duration ): Duration =
        if ( validOptions == null ) interval
        else checkNotNull( validOptions.minByOrNull { (interval - it).absoluteValue } )

    override fun build(): IntervalSamplingConfiguration = IntervalSamplingConfiguration( interval )
}
