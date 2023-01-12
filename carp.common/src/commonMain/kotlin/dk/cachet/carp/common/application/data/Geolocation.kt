package dk.cachet.carp.common.application.data

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Holds geolocation data as latitude and longitude in decimal degrees within the World Geodetic System 1984.
 */
@Serializable
@SerialName( CarpDataTypes.GEOLOCATION_TYPE_NAME )
@JsExport
data class Geolocation(
    val latitude: Double,
    val longitude: Double,
    override val sensorSpecificData: Data? = null
) : SensorData
{
    companion object
    {
        const val MIN_LATITUDE: Double = -90.0
        const val MAX_LATITUDE: Double = 90.0
        const val MIN_LONGITUDE: Double = -180.0
        const val MAX_LONGITUDE: Double = 180.0
    }

    init
    {
        require( latitude in MIN_LATITUDE..MAX_LATITUDE )
            { "Latitude needs to lie between -90 and 90 decimal degrees." }
        require( longitude in MIN_LONGITUDE..MAX_LONGITUDE )
            { "Longitude needs to lie between -180 and 180 decimal degrees." }
    }
}
