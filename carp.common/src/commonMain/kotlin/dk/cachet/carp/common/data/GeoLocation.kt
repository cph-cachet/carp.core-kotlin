package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds geolocation data as latitude and longitude in decimal degrees within the World Geodetic System 1984.
 */
@Serializable
@SerialName( GEOLOCATION_TYPE_NAME )
data class GeoLocation( val latitude: Double, val longitude: Double )
