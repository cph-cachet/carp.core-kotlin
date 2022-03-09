package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * A wrapper used to load extending types from [TriggerConfiguration] serialized as JSON which are unknown at runtime.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( TriggerConfigurationSerializer::class )
data class CustomTriggerConfiguration(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : TriggerConfiguration<NoData>(), UnknownPolymorphicWrapper
{
    @Serializable
    private data class BaseMembers( override val sourceDeviceRoleName: String ) : TriggerConfiguration<NoData>()

    override val sourceDeviceRoleName: String

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        sourceDeviceRoleName = baseMembers.sourceDeviceRoleName
    }
}

/**
 * Custom serializer for a [TriggerConfiguration] which enables deserializing types that are unknown at runtime, yet extend from [TriggerConfiguration].
 */
object TriggerConfigurationSerializer : KSerializer<TriggerConfiguration<*>>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomTriggerConfiguration( className, json, serializer ) } )
