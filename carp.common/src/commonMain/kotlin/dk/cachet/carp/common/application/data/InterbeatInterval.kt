package dk.cachet.carp.common.application.data

import kotlinx.serialization.*


/**
 * The associated time interval denotes the interval between two consecutive heartbeats.
 * E.g., a measured RR interval.
 */
@Serializable
@SerialName( CarpDataTypes.INTERBEAT_INTERVAL_TYPE_NAME )
data class InterbeatInterval( override val sensorSpecificData: Data? = null ) : SensorData
