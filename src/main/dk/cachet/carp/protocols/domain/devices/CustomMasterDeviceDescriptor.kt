package dk.cachet.carp.protocols.domain.devices

import com.beust.klaxon.*
import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper


/**
 * A wrapper used to load extending types from [MasterDeviceDescriptor]s serialized as JSON which are unknown at runtime.
 */
data class CustomMasterDeviceDescriptor( override val className: String, override val jsonSource: String )
    : MasterDeviceDescriptor(), UnknownPolymorphicWrapper
{
    override val roleName: String

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        val roleNameField = MasterDeviceDescriptor::roleName.name
        roleName = json.string( roleNameField ) ?: throw IllegalArgumentException( "No '$roleNameField' defined." )
    }
}