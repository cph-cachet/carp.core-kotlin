package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.*
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [DeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json )
    : DeviceDescriptor<DeviceRegistrationBuilder>(), UnknownPolymorphicWrapper
{
    override val roleName: String

    init
    {
        val json = serializer.parseJson( jsonSource ) as JsonObject

        val roleNameField = DeviceDescriptor<*>::roleName.name
        if ( !json.containsKey( roleNameField ) )
        {
            throw IllegalArgumentException( "No '$roleNameField' defined." )
        }
        roleName = json[ roleNameField ]!!.content
    }
    
    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder
        = throw UnsupportedOperationException( "The concrete type of this device is not known. Therefore, it is unknown which registration builder is required." )

    /**
     * For unknown types, it cannot be determined whether or not a given registration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}