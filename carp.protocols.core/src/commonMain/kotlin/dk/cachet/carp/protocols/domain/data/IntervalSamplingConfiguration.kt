package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.TimeSpan
import kotlinx.serialization.Serializable


/**
 * A sampling configuration which allows configuring the time [interval] in between subsequent measurements.
 */
@Serializable
data class IntervalSamplingConfiguration( val interval: TimeSpan ) : SamplingConfiguration()

