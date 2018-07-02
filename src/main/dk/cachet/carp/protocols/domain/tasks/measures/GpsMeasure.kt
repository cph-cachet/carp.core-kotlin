package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.Serializable


/**
 * Measure the GPS location as determined by the device this measure is requested on.
 */
@Serializable
data class GpsMeasure( override val type: DataType = GpsDataType() ) : DataStreamMeasure()