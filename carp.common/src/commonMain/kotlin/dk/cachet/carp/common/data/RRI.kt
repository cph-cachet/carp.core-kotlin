package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds RRI in ms
 */
@Serializable
@SerialName( CarpDataTypes.RRI_TYPE_NAME )
data class RRI(val intervalMs: Int ) : Data
