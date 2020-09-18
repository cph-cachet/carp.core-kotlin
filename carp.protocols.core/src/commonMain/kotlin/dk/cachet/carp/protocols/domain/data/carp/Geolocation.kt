package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.data.GEOLOCATION_TYPE
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfigurationBuilder


/**
 * Sampling scheme for geographic location data, representing longitude and latitude.
 */
class Geolocation(
    val defaultMeasureInterval: TimeSpan
) : DataTypeSamplingScheme<GeolocationSamplingConfigurationBuilder>( GEOLOCATION_TYPE )
{
    override fun createSamplingConfigurationBuilder(): GeolocationSamplingConfigurationBuilder =
        GeolocationSamplingConfigurationBuilder( defaultMeasureInterval )
}

typealias GeolocationSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder
