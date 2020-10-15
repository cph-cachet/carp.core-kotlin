package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Indicates the corresponding time interval represents the time between two consecutive heartbeats (R-R interval).
 */
@Serializable
@SerialName( CarpDataTypes.RR_INTERVAL_TYPE_NAME )
object RRInterval : Data
