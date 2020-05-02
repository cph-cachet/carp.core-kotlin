package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.DataTypeMetadata
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
    companion object Factory
    {
        private fun <T : DataTypeMetadata<*>> measureOf( type: T, duration: TimeSpan ) = PhoneSensorMeasure( type.TYPE, duration )

        /**
         * Measure geographic location data (longitude and latitude).
         */
        fun geolocation( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( Geolocation, duration )

        /**
         * Measure amount of steps a participant has taken in a specified time interval.
         */
        fun stepcount( duration: TimeSpan = TimeSpan.INFINITE ) = measureOf( Stepcount, duration )

        /**
         * All the data types of sensor measures commonly supported on smartphones supported by this factory.
         */
        val supportedDataTypes = arrayOf( Geolocation, Stepcount )
    }


    init
    {
        // Since supported sensors by CARP should co-evolve across the platform (measure definitions and matching probe implementations),
        // only data types that are supported are allowed. If new probes are implemented for PhoneSensorMeasure, this class should be updated correspondingly.
        // TODO: This is currently 'somewhat' enforced using a private constructor. But, 'copy' can still be used.
        require( type in supportedDataTypes.map { it.TYPE } ) { "Invalid data type passed to ${PhoneSensorMeasure::class.simpleName}." }
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
        addConfiguration( Geolocation, builder )

    /**
     * Configure sampling configuration for [Stepcount].
     */
    fun stepcount( builder: StepcountSamplingConfigurationBuilder.() -> Unit ): SamplingConfiguration =
        addConfiguration( Stepcount, builder )
}
