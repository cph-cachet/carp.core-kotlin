package dk.cachet.carp.common.application.data

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Holds heart rate data in beats per minute ([bpm]).
 */
@Serializable
@SerialName( CarpDataTypes.HEART_RATE_TYPE_NAME )
@JsExport
data class HeartRate( val bpm: Int, override val sensorSpecificData: Data? = null ) : SensorData
{
    init
    {
        require( bpm >= 0 ) { "Beats per minute needs to be a positive number." }
    }
}
