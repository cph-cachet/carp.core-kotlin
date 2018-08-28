package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [Trigger] serialized as JSON which are unknown at runtime.
 */
data class CustomTrigger( override val className: String, override val jsonSource: String )
    : Trigger(), UnknownPolymorphicWrapper
{
    override val sourceDeviceRoleName: String

    init
    {
        val parser = JsonTreeParser( jsonSource )
        val json = parser.readFully() as JsonObject

        val sourceDeviceRoleNameField = Trigger::sourceDeviceRoleName.name
        if ( !json.containsKey( sourceDeviceRoleNameField ) )
        {
            throw IllegalArgumentException( "No '$sourceDeviceRoleNameField' defined." )
        }
        sourceDeviceRoleName = json[ sourceDeviceRoleNameField ].content
    }
}