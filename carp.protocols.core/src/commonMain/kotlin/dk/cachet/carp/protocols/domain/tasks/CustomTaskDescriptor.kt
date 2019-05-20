package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [TaskDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomTaskDescriptor( override val className: String, override val jsonSource: String )
    : TaskDescriptor(), UnknownPolymorphicWrapper
{
    override val name: String
    override val measures: List<Measure>

    init
    {
        val json = Json.plain.parseJson( jsonSource ) as JsonObject

        val nameField = TaskDescriptor::name.name
        if ( !json.containsKey( nameField ) )
        {
            throw IllegalArgumentException( "No '$nameField' defined." )
        }
        name = json[ nameField ]!!.content

        // Get raw JSON string of measures and use kotlinx serialization to deserialize.
        val measuresField = TaskDescriptor::measures.name
        if ( !json.containsKey( measuresField ) )
        {
            throw IllegalArgumentException( "No '$measuresField' defined." )
        }
        val measuresJson = json[ measuresField ]!!.jsonArray.toString()
        measures = Json.parse( MeasuresSerializer, measuresJson )
    }
}