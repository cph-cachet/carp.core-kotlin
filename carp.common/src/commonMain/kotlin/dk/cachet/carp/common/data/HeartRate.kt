package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds heart rate data in beats per minute ([bpm]).
 */
@Serializable
@SerialName( CarpDataTypes.HEART_RATE_TYPE_NAME )
data class HeartRate( val bpm: Int ) : Data
