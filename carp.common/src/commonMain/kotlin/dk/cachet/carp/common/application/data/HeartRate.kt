@file:JsExport

package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Holds heart rate data in beats per minute ([bpm]).
 */
@Serializable
@SerialName( CarpDataTypes.HEART_RATE_TYPE_NAME )
data class HeartRate( val bpm: Int ) : Data
