package dk.cachet.carp.common.application.data

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * The relative received signal strength of a wireless device.
 * The unit of the received signal strength indicator ([rssi]) is arbitrary and determined by the chip manufacturer,
 * but the greater the value, the stronger the signal.
 */
@Serializable
@SerialName( CarpDataTypes.SIGNAL_STRENGTH_TYPE_NAME )
@JsExport
data class SignalStrength( val rssi: Short, override val sensorSpecificData: Data? = null ) : SensorData
