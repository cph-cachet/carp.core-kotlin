package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfigurationBuilder


/**
 * Sampling scheme for geographic location data, representing longitude and latitude.
 */
class Geolocation(
    val defaultMeasureInterval: TimeSpan
) : DataTypeSamplingScheme<GeolocationSamplingConfigurationBuilder>( carpDataType( "geolocation" ) )
{
    override fun createSamplingConfigurationBuilder(): GeolocationSamplingConfigurationBuilder =
        GeolocationSamplingConfigurationBuilder( defaultMeasureInterval )
}

typealias GeolocationSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder
