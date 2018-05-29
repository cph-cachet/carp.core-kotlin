package dk.cachet.carp.protocols.domain.devices

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser


/**
 * A wrapper used to load extending types from [MasterDeviceDescriptor]s serialized as JSON which are unknown at runtime.
 */
internal data class CustomMasterDeviceDescriptor( val className: String, val jsonSource: String ) : MasterDeviceDescriptor()
{
    override val roleName: String

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        val roleNameField = MasterDeviceDescriptor::roleName.name
        roleName = json.string( roleNameField ) ?: throw IllegalArgumentException( "No '$roleNameField' defined." )
    }
}