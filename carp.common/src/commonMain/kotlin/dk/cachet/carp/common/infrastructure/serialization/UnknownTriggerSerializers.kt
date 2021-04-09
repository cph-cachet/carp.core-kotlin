package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.triggers.Trigger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * A wrapper used to load extending types from [Trigger] serialized as JSON which are unknown at runtime.
 */
@Serializable( TriggerSerializer::class )
data class CustomTrigger( override val className: String, override val jsonSource: String, val serializer: Json ) :
    Trigger(), UnknownPolymorphicWrapper
{
    @Serializable
    private data class BaseMembers( override val sourceDeviceRoleName: String ) : Trigger()

    override val sourceDeviceRoleName: String

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        sourceDeviceRoleName = baseMembers.sourceDeviceRoleName
    }
}

/**
 * Custom serializer for a [Trigger] which enables deserializing types that are unknown at runtime, yet extend from [Trigger].
 */
object TriggerSerializer : KSerializer<Trigger>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomTrigger( className, json, serializer ) } )
