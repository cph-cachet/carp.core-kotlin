package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingSchemeList
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.data.SamplingConfigurationMapBuilder
import dk.cachet.carp.protocols.domain.data.carp.Geolocation
import dk.cachet.carp.protocols.domain.data.carp.GeolocationSamplingConfigurationBuilder
import dk.cachet.carp.protocols.domain.data.carp.Stepcount
import dk.cachet.carp.protocols.domain.data.carp.StepcountSamplingConfigurationBuilder
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import kotlinx.serialization.Serializable


/**
 * Measures any of the sensors typically integrated in smartphones (e.g., accelerometer),
 * or data which is derived from them using vendor-specific APIs (e.g., stepcount, or mode of transport).
 */
@Suppress( "DataClassPrivateConstructor" )
@Serializable
data class PhoneSensorMeasure private constructor(
    override val type: DataType,
    /**
     * The optional duration over the course of which the sensor identified by [type] needs to be measured.
     * Infinite by default.
     */
    val duration: TimeSpan = TimeSpan.INFINITE
) : Measure()
{
    /**
     * All the data types and sampling schemes of sensor measures commonly supported on smartphones, supported by [PhoneSensorMeasure].
     */
    object SamplingSchemes : DataTypeSamplingSchemeList()
    {
        val GEOLOCATION = add( Geolocation( TimeSpan.fromMinutes( 1.0 ) ) )
        val STEPCOUNT = add( Stepcount( TimeSpan.fromMinutes( 1.0 ) ) )
    }

    companion object Factory
    {
        private fun <T : DataTypeSamplingScheme<*>> measureOf( samplingScheme: T, duration: TimeSpan ) =
            PhoneSensorMeasure( samplingScheme.type, duration )

        /**
         * Measure geographic location data (longitude and latitude).
         */
        fun geolocation( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( SamplingSchemes.GEOLOCATION, duration )

        /**
         * Measure amount of steps a participant has taken in a specified time interval.
         */
        fun stepcount( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( SamplingSchemes.STEPCOUNT, duration )
    }


    init
    {
        // Since supported sensors by CARP should co-evolve across the platform (measure definitions and matching probe implementations),
        // only data types that are supported are allowed. If new probes are implemented for PhoneSensorMeasure, this class should be updated correspondingly.
        // TODO: This is currently 'somewhat' enforced using a private constructor. But, 'copy' can still be used.
        require( type in SamplingSchemes.map { it.type } ) { "Invalid data type passed to ${PhoneSensorMeasure::class.simpleName}." }
    }
}


/**
 * A helper class to configure and construct immutable [SamplingConfiguration] classes for [PhoneSensorMeasure]s
 * as part of setting up a [DeviceDescriptor].
 */
class PhoneSensorSamplingConfigurationMapBuilder : SamplingConfigurationMapBuilder()
{
    /**
     * Configure sampling configuration for [Geolocation].
     */
    fun geolocation( builder: GeolocationSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        addConfiguration( PhoneSensorMeasure.SamplingSchemes.GEOLOCATION, builder )

    /**
     * Configure sampling configuration for [Stepcount].
     *
     * TODO: Android can both 'listen' for steps (delay of about 2 s), and poll for steps (latency of about 10 s, but more accurate).
     *       Should we add a separate data type (STEPCOUNT_LISTENER) for the listener, or choose one or the other option in configuration?
     *       How does this align with iPhone?
     * Android (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter):
     * - There is a latency of up to 10 s.
     */
    fun stepcount( builder: StepcountSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        addConfiguration( PhoneSensorMeasure.SamplingSchemes.STEPCOUNT, builder )
}
