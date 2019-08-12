package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [TaskDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomTaskDescriptor( override val className: String, override val jsonSource: String, val serializer: Json )
    : TaskDescriptor(), UnknownPolymorphicWrapper
{
    override val name: String
    override val measures: List<Measure>

    init
    {
        val json = serializer.parseJson( jsonSource ) as JsonObject

        val nameField = TaskDescriptor::name.name
        require( json.containsKey( nameField ) ) { "No '$nameField' defined." }
        name = json[ nameField ]!!.content

        // Get raw JSON string of measures and use kotlinx serialization to deserialize.
        val measuresField = TaskDescriptor::measures.name
        require( json.containsKey( measuresField ) ) { "No '$measuresField' defined." }
        val measuresJson = json[ measuresField ]!!.jsonArray.toString()
        measures = serializer.parse( MeasuresSerializer, measuresJson )
    }
}

/**
 * Custom serializer for a list of [TaskDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [TaskDescriptor].
 */
@Suppress( "RemoveExplicitTypeArguments" ) // Removing this fails compilation. Might be a bug in the analyzer.
object TasksSerializer : KSerializer<List<TaskDescriptor>> by ArrayListSerializer<TaskDescriptor>(
    createUnknownPolymorphicSerializer { className, json, serializer -> CustomTaskDescriptor( className, json, serializer ) }
)

/**
 * Custom serializer for a set of [TaskDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [TaskDescriptor].
 */
@Suppress( "RemoveExplicitTypeArguments" ) // Removing this fails compilation. Might be a bug in the analyzer.
object TasksSetSerializer : KSerializer<Set<TaskDescriptor>> by HashSetSerializer<TaskDescriptor>(
    createUnknownPolymorphicSerializer { className, json, serializer -> CustomTaskDescriptor( className, json, serializer ) }
)


/**
 * A wrapper used to load extending types from [Measure] serialized as JSON which are unknown at runtime.
 */
data class CustomMeasure( override val className: String, override val jsonSource: String, val serializer: Json)
    : Measure(), UnknownPolymorphicWrapper
{
    override val type: DataType

    init
    {
        val json = serializer.parseJson( jsonSource ) as JsonObject

        // Get raw JSON string of type (using klaxon) and use kotlinx serialization to deserialize.
        val typeField = Measure::type.name
        require( json.containsKey( typeField ) ) { "No '$typeField' defined." }
        val typeJson = json[ typeField ]!!.jsonObject.toString()
        type = serializer.parse( DataType.serializer(), typeJson )
    }
}

/**
 * Custom serializer for a list of [Measure]s which enables deserializing types that are unknown at runtime, yet extend from [Measure].
 */
@Suppress( "RemoveExplicitTypeArguments" ) // Removing this fails compilation. Might be a bug in the analyzer.
object MeasuresSerializer : KSerializer<List<Measure>> by ArrayListSerializer<Measure>(
    createUnknownPolymorphicSerializer { className, json, serializer -> CustomMeasure( className, json, serializer ) }
)