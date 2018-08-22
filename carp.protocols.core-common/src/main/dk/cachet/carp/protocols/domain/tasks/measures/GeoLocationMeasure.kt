package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.serialization.Serializable


/**
 * Measure the geographic location (longitude and latitude) as determined by the device this measure is requested on.
 */
@Serializable
data class GeoLocationMeasure( override val type: DataType = GeoLocationDataType() ) : DataStreamMeasure()