package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds rate of rotation around perpendicular [x], [y], and [z] axes in radians per second.
 * Positive angular velocity indicates counter-clockwise rotation from the perspective of an observer
 * at some positive location on the x, y, or z axis, while negative angular velocity indicates clockwise rotation.
 */
@Serializable
@SerialName( CarpDataTypes.ANGULAR_VELOCITY_TYPE_NAME )
data class AngularVelocity(
    val x: Double,
    val y: Double,
    val z: Double,
    override val sensorSpecificData: Data? = null
) : SensorData
