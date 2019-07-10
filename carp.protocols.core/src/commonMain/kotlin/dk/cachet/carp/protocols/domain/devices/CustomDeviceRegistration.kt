package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.*
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [DeviceRegistration] serialized as JSON which are unknown at runtime.
 */
data class CustomDeviceRegistration( override val className: String, override val jsonSource: String )
    : DeviceRegistration(), UnknownPolymorphicWrapper
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    override val deviceId: String

    init
    {
        val json = JSON.parseJson( jsonSource ) as JsonObject

        val deviceIdField = DeviceRegistration::deviceId.name
        if ( !json.containsKey( deviceIdField ) )
        {
            throw IllegalArgumentException( "No '$deviceIdField' defined." )
        }
        deviceId = json[ deviceIdField ]!!.content
    }
}