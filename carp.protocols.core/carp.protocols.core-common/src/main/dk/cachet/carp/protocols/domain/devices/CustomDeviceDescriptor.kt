package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [DeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomDeviceDescriptor( override val className: String, override val jsonSource: String )
    : DeviceDescriptor(), UnknownPolymorphicWrapper
{
    override val roleName: String

    init
    {
        val parser = JsonTreeParser( jsonSource )
        val json = parser.readFully() as JsonObject

        val roleNameField = DeviceDescriptor::roleName.name
        if ( !json.containsKey( roleNameField ) )
        {
            throw IllegalArgumentException( "No '$roleNameField' defined." )
        }
        roleName = json[ roleNameField ].content
    }

    /**
     * For unknown types, it cannot be determined whether or not a given registration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}