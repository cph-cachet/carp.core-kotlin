package dk.cachet.carp.protocols.domain.sampling.carp

import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.protocols.domain.sampling.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.sampling.IntervalSamplingConfigurationBuilder


/**
 * Sampling scheme for geographic location data, representing longitude and latitude in decimal degrees within the World Geodetic System 1984.
 */
class GeolocationSamplingScheme(
    val defaultMeasureInterval: TimeSpan
) : DataTypeSamplingScheme<GeolocationSamplingConfigurationBuilder>( CarpDataTypes.GEOLOCATION )
{
    override fun createSamplingConfigurationBuilder(): GeolocationSamplingConfigurationBuilder =
        GeolocationSamplingConfigurationBuilder( defaultMeasureInterval )
}

typealias GeolocationSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder
