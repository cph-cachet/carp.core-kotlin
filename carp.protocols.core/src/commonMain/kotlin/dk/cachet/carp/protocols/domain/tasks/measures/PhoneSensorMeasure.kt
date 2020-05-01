package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfigurationBuilder
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.data.carp.GEO_LOCATION as GEOLOCATION
import dk.cachet.carp.protocols.domain.data.carp.STEPCOUNT as STEPCOUNT
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptorBuilderDsl
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
    companion object Factory
    {
        /**
         * Measure geographic location data (longitude and latitude).
         */
        fun geolocation( duration: TimeSpan = TimeSpan.INFINITE ) = PhoneSensorMeasure( GEOLOCATION, duration )

        val DEFAULT_STEPCOUNT_INTERVAL: TimeSpan = TimeSpan.fromMinutes( 1.0 )
        /**
         * Measure amount of steps a participant has taken (measured per time interval).
         *
         * TODO: Android can both 'listen' for steps (delay of about 2 s), and poll for steps (latency of about 10 s, but more accurate).
         *       Should we add a separate data type (STEPCOUNT_LISTENER) for the listener, or choose one or the other option in configuration?
         *       How does this align with iPhone?
         * Android (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter):
         * - There is a latency of up to 10 s.
         */
        fun stepcount( duration: TimeSpan = TimeSpan.INFINITE ) = PhoneSensorMeasure( STEPCOUNT, duration )

        /**
         * All the data types of sensor measures commonly supported on smartphones supported by this factory.
         */
        val supportedDataTypes get() = arrayOf( GEOLOCATION, STEPCOUNT )

        /**
         * A builder function to configure and construct [SamplingConfiguration]s for all [PhoneSensorMeasure]s.
         */
        fun createSamplingConfiguration( builder: PhoneSensorSamplingConfigurationBuilder.() -> Unit ): Map<DataType, SamplingConfiguration> =
            PhoneSensorSamplingConfigurationBuilder().apply( builder ).build()
    }


    init
    {
        // Since supported sensors by CARP should co-evolve across the platform (measure definitions and matching probe implementations),
        // only data types that are supported are allowed. If new probes are implemented for PhoneSensorMeasure, this class should be updated correspondingly.
        // TODO: This is currently 'somewhat' enforced using a private constructor. But, 'copy' can still be used.
        require( supportedDataTypes.contains( type ) ) { "Invalid data type passed to ${PhoneSensorMeasure::class.simpleName}." }
    }
}


/**
 * A helper class to configure and construct immutable [SamplingConfiguration] classes for [PhoneSensorMeasure]s
 * as part of setting up a [DeviceDescriptor].
 */
@DeviceDescriptorBuilderDsl
class PhoneSensorSamplingConfigurationBuilder
{
    private val samplingConfigurations: MutableMap<DataType, SamplingConfiguration> = mutableMapOf()

    fun stepcount( builder: IntervalSamplingConfigurationBuilder.() -> Unit )
    {
        val configuration = IntervalSamplingConfigurationBuilder( PhoneSensorMeasure.DEFAULT_STEPCOUNT_INTERVAL )
            .apply( builder ).build()
        samplingConfigurations[ STEPCOUNT ] = configuration
    }

    fun build(): Map<DataType, SamplingConfiguration> = samplingConfigurations.toMap()
}
