package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
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
    private class BaseMembers( override val name: String, override val measures: List<Measure> ) : TaskDescriptor

    override val name: String
    override val measures: List<Measure>

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


/**
 * A wrapper used to load extending types from [Measure] serialized as JSON which are unknown at runtime.
 */
@Serializable( MeasureSerializer::class )
data class CustomMeasure( override val className: String, override val jsonSource: String, val serializer: Json ) :
    Measure, UnknownPolymorphicWrapper
{
    @Serializable
    private class BaseMembers( override val type: DataType ) : Measure

    override val type: DataType

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        type = baseMembers.type
    }
}

/**
 * Custom serializer for [Measure] which enables deserializing types that are unknown at runtime, yet extend from [Measure].
 */
object MeasureSerializer : KSerializer<Measure>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomMeasure( className, json, serializer ) } )
