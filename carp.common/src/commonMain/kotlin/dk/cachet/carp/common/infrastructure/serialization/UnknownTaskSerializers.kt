package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


/**
 * A wrapper used to load extending types from [TaskConfiguration] serialized as JSON which are unknown at runtime.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( TaskConfigurationSerializer::class )
data class CustomTaskConfiguration internal constructor(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : TaskConfiguration<NoData>, UnknownPolymorphicWrapper
{
    @Serializable
    private data class BaseMembers(
        override val name: String,
        override val measures: List<Measure> = emptyList(),
        override val description: String? = null
    ) : TaskConfiguration<NoData>

    override val name: String
    override val measures: List<Measure>
    override val description: String? = null

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        name = baseMembers.name
        measures = baseMembers.measures
    }
}

/**
 * Custom serializer for [TaskConfiguration] which enables deserializing types that are unknown at runtime, yet extend from [TaskConfiguration].
 */
object TaskConfigurationSerializer : KSerializer<TaskConfiguration<*>> by createUnknownPolymorphicSerializer(
    { className, json, serializer -> CustomTaskConfiguration( className, json, serializer ) }
)
