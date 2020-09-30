package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds heart rate data in beats per minute.
 */
@Serializable
@SerialName( CarpDataTypes.HEARTRATE_TYPE_NAME )
data class HeartRate( val beatsPerMinute: Float ) : Data
