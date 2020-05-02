package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataTypeMetadata
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfigurationBuilder


/**
 * Amount of steps a participant has taken in a specified time interval.
 *
 * TODO: Android can both 'listen' for steps (delay of about 2 s), and poll for steps (latency of about 10 s, but more accurate).
 *       Should we add a separate data type (STEPCOUNT_LISTENER) for the listener, or choose one or the other option in configuration?
 *       How does this align with iPhone?
 * Android (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter):
 * - There is a latency of up to 10 s.
 */
object Stepcount : DataTypeMetadata<StepcountSamplingConfigurationBuilder>( carpDataType( "stepcount" ) )
{
    val DEFAULT_MEASURE_INTERVAL: TimeSpan = TimeSpan.fromMinutes( 1.0 )

    override fun createSamplingConfigurationBuilder(): StepcountSamplingConfigurationBuilder =
        StepcountSamplingConfigurationBuilder( DEFAULT_MEASURE_INTERVAL )
}

typealias StepcountSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder
