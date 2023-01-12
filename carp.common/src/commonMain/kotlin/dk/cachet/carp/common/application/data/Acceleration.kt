package dk.cachet.carp.common.application.data

import kotlinx.serialization.*
import kotlin.js.JsExport

/**
 * Holds rate of change in velocity, including gravity, along perpendicular [x], [y], and [z] axes in meters per second squared (m/s^2).
 */
@Serializable
@SerialName( CarpDataTypes.ACCELERATION_TYPE_NAME )
@JsExport
data class Acceleration(
    val x: Double,
    val y: Double,
    val z: Double,
    override val sensorSpecificData: Data? = null
) : SensorData
