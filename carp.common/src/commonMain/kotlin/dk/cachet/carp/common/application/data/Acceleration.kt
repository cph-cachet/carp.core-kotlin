package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Holds rate of change in velocity, including gravity, along perpendicular [x], [y], and [z] axes in meters per second squared (m/s^2).
 */
@Serializable
@SerialName( CarpDataTypes.ACCELERATION_TYPE_NAME )
data class Acceleration( val x: Double, val y: Double, val z: Double ) : Data
