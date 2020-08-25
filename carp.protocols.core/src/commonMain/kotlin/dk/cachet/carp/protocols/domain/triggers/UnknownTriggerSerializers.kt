package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive


/**
 * A wrapper used to load extending types from [Trigger] serialized as JSON which are unknown at runtime.
 */
data class CustomTrigger( override val className: String, override val jsonSource: String, val serializer: Json ) :
    Trigger(), UnknownPolymorphicWrapper
{
    override val sourceDeviceRoleName: String

    init
    {
        val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

        val sourceDeviceRoleNameField = Trigger::sourceDeviceRoleName.name
        require( json.containsKey( sourceDeviceRoleNameField ) ) { "No '$sourceDeviceRoleNameField' defined." }
        sourceDeviceRoleName = json[ sourceDeviceRoleNameField ]!!.jsonPrimitive.content
    }
}

/**
 * Custom serializer for a [Trigger] which enables deserializing types that are unknown at runtime, yet extend from [Trigger].
 */
object TriggerSerializer : KSerializer<Trigger>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomTrigger( className, json, serializer ) } )
