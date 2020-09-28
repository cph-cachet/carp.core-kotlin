package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName( HEARTRATE_TYPE_NAME )
data class HeartRate( val bpm: Float) : Data
