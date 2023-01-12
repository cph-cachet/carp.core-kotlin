package dk.cachet.carp.common.application.data

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Holds data on whether a sensor requiring contact with skin is making proper contact.
 */
@Serializable
@SerialName( CarpDataTypes.SENSOR_SKIN_CONTACT_TYPE_NAME )
@JsExport
data class SensorSkinContact( val contact: Boolean, override val sensorSpecificData: Data? = null ) : SensorData
