package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
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