package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds electrocardiogram data of a single lead.
 */
@Serializable
@SerialName( CarpDataTypes.ECG_TYPE_NAME )
data class ECG( val milliVolt: Double, override val sensorSpecificData: Data? = null ) : SensorData
