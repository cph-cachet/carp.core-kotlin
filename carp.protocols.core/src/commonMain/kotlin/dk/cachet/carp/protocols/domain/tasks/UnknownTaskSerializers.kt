package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


/**
 * A wrapper used to load extending types from [TaskDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomTaskDescriptor( override val className: String, override val jsonSource: String, val serializer: Json ) :
    TaskDescriptor, UnknownPolymorphicWrapper
{
    override val name: String
    override val measures: List<Measure>

    init
    {
        val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

        val nameField = TaskDescriptor::name.name
        require( json.containsKey( nameField ) ) { "No '$nameField' defined." }
        name = json[ nameField ]!!.jsonPrimitive.content

        // Get raw JSON string of measures and use kotlinx serialization to deserialize.
        val measuresField = TaskDescriptor::measures.name
        require( json.containsKey( measuresField ) ) { "No '$measuresField' defined." }
        val measuresJson = json[ measuresField ]!!.jsonArray.toString()
        measures = serializer.decodeFromString( MeasuresSerializer, measuresJson )
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
data class CustomMeasure( override val className: String, override val jsonSource: String, val serializer: Json ) :
    Measure, UnknownPolymorphicWrapper
{
    override val type: DataType

    init
    {
        val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

        // Get raw JSON string of type (using klaxon) and use kotlinx serialization to deserialize.
        val typeField = Measure::type.name
        require( json.containsKey( typeField ) ) { "No '$typeField' defined." }
        val typeJson = json[ typeField ]!!.jsonObject.toString()
        type = serializer.decodeFromString( DataType.serializer(), typeJson )
    }
}

/**
 * Custom serializer for [Measure] which enables deserializing types that are unknown at runtime, yet extend from [Measure].
 */
object MeasureSerializer : KSerializer<Measure>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomMeasure( className, json, serializer ) } )

/**
 * Custom serializer for a list of [Measure]s which enables deserializing types that are unknown at runtime, yet extend from [Measure].
 */
object MeasuresSerializer : KSerializer<List<Measure>> by ListSerializer( MeasureSerializer )
