package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.tasks.Measure
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * A wrapper used to load extending types from [TaskDescriptor] serialized as JSON which are unknown at runtime.
 */
@Serializable( TaskDescriptorSerializer::class )
data class CustomTaskDescriptor( override val className: String, override val jsonSource: String, val serializer: Json ) :
    TaskDescriptor, UnknownPolymorphicWrapper
{
    @Serializable
    private data class BaseMembers(
        override val name: String,
        override val measures: List<Measure> = emptyList(),
        override val description: String? = null
    ) : TaskDescriptor

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
 * Custom serializer for [TaskDescriptor] which enables deserializing types that are unknown at runtime, yet extend from [TaskDescriptor].
 */
object TaskDescriptorSerializer : KSerializer<TaskDescriptor>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomTaskDescriptor( className, json, serializer ) } )
