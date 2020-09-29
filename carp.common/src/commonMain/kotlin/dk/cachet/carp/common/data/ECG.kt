package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds electrocardiogram data of a single lead.
 */
@Serializable
@SerialName( ECG_TYPE_NAME )
data class ECG( val milliVolt: Double ) : Data
