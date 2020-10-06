package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName( CarpDataTypes.SENSORCONTACT_TYPE_NAME )
data class SensorContact( val contact: Boolean ) : Data
