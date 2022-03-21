package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Holds change in velocity excluding gravity along perpendicular [x], [y], and [z] axes in meters per second squared (m/s^2).
 */
@Serializable
@SerialName( CarpDataTypes.NON_GRAVITATIONAL_ACCELERATION_TYPE_NAME )
data class NonGravitationalAcceleration( val x: Double, val y: Double, val z: Double ) : Data
