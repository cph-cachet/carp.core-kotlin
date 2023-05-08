package dk.cachet.carp.common.application.data

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Holds electrocardiogram data of a single lead.
 */
@Serializable
@SerialName( CarpDataTypes.ECG_TYPE_NAME )
@JsExport
data class ECG( val milliVolt: Double, override val sensorSpecificData: Data? = null ) : SensorData
