package dk.cachet.carp.protocols.domain.data.carp

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfigurationBuilder


/**
 * Sampling scheme for stepcount data, representing the amount of steps a participant has taken in a specified time interval.
 */
class Stepcount(
    val defaultMeasureInterval: TimeSpan
) : DataTypeSamplingScheme<StepcountSamplingConfigurationBuilder>( carpDataType( "stepcount" ) )
{
    override fun createSamplingConfigurationBuilder(): StepcountSamplingConfigurationBuilder =
        StepcountSamplingConfigurationBuilder( defaultMeasureInterval )
}

typealias StepcountSamplingConfigurationBuilder = IntervalSamplingConfigurationBuilder
