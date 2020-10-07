package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName( CarpDataTypes.ACCELEROMETER_TYPE_NAME )
data class Accelerometer( val gx: Double, val gy: Double, val gz: Double ) : Data
