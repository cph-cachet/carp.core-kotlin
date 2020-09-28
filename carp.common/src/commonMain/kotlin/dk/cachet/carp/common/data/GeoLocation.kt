package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName( GEOLOCATION_TYPE_NAME )
data class GeoLocation( val latitude: Double, val longitude: Double )
