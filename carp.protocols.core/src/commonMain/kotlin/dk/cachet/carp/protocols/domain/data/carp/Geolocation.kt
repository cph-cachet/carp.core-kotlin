package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataTypeMetadata
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfigurationBuilder


/**
 * Geographic location data: longitude and latitude.
 */
object Geolocation : DataTypeMetadata<GeolocationSamplingConfigurationBuilder>( carpDataType( "geolocation" ) )
{
    val DEFAULT_MEASURE_INTERVAL: TimeSpan = TimeSpan.fromMinutes( 1.0 )

    override fun createSamplingConfigurationBuilder(): GeolocationSamplingConfigurationBuilder =
        GeolocationSamplingConfigurationBuilder( DEFAULT_MEASURE_INTERVAL )
}

typealias GeolocationSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder
