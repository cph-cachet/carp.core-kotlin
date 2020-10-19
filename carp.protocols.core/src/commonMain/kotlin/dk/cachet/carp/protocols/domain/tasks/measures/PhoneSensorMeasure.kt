package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.sampling.SamplingConfigurationMapBuilder
import dk.cachet.carp.protocols.domain.sampling.carp.GeolocationSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.carp.GeolocationSamplingConfigurationBuilder
import dk.cachet.carp.protocols.domain.sampling.carp.StepCountSamplingScheme
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
) : Measure
{
    /**
     * All the data types and sampling schemes of sensor measures commonly supported on smartphones, supported by [PhoneSensorMeasure].
     */
    object SamplingSchemes : DataTypeSamplingSchemeList()
    {
        val GEOLOCATION = add( GeolocationSamplingScheme( TimeSpan.fromMinutes( 1.0 ) ) )

        /**
         * Steps within recorded time intervals as reported by a phone's dedicated hardware sensor.
         * Data rate is determined by the sensor.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter):
         * - There is a latency of up to 10 s.
         * - Only available starting from Android 4.4.
         *
         * TODO: Android can also 'listen' for steps, which has a delay of about 2 s but is less accurate.
         *       Each 'step' is reported as an event, so this would map to a different DataType (e.g. `Step`).
         *       Not certain this is available on iPhone.
         */
        val STEP_COUNT = add( StepCountSamplingScheme ) // No configuration options available.
    }

    companion object
    {
        private fun <T : DataTypeSamplingScheme<*>> measureOf( samplingScheme: T, duration: TimeSpan ) =
            PhoneSensorMeasure( samplingScheme.type, duration )

        /**
         * Measure geographic location data (longitude and latitude).
         */
        fun geolocation( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( SamplingSchemes.GEOLOCATION, duration )

        /**
         * Measure number of steps a participant has taken in a recorded time interval.
         */
        fun stepCount( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( SamplingSchemes.STEP_COUNT, duration )
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
     * Configure sampling configuration for [GeolocationSamplingScheme].
     */
    fun geolocation( builder: GeolocationSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        addConfiguration( PhoneSensorMeasure.SamplingSchemes.GEOLOCATION, builder )
}
