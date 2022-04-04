package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * The associated time interval denotes the interval between two consecutive heartbeats.
 * E.g., a measured RR interval.
 */
@Serializable
@SerialName( CarpDataTypes.INTERBEAT_INTERVAL_TYPE_NAME )
object InterbeatInterval : Data
