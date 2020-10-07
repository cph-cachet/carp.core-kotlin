package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Holds acceleration data for x, y and z axis in G.
 */
@Serializable
@SerialName( CarpDataTypes.ACCELERATION_TYPE_NAME )
data class Acceleration(val x: Double, val y: Double, val z: Double ) : Data
