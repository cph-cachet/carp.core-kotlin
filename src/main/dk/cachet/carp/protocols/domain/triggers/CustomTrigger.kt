package dk.cachet.carp.protocols.domain.triggers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper


data class CustomTrigger( override val className: String, override val jsonSource: String )
    : Trigger(), UnknownPolymorphicWrapper
{
    override val sourceDeviceRoleName: String

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        val sourceDeviceRoleNameField = Trigger::sourceDeviceRoleName.name
        sourceDeviceRoleName = json.string( sourceDeviceRoleNameField ) ?: throw IllegalArgumentException( "No '$sourceDeviceRoleNameField' defined." )
    }
}