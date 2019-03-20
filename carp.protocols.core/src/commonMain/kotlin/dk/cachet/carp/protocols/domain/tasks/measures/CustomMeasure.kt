package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [Measure] serialized as JSON which are unknown at runtime.
 */
data class CustomMeasure( override val className: String, override val jsonSource: String ) : Measure(), UnknownPolymorphicWrapper
{
    override val type: DataType

    init
    {
        val json = Json.plain.parseJson( jsonSource ) as JsonObject

        // Get raw JSON string of type (using klaxon) and use kotlinx serialization to deserialize.
        val typeField = Measure::type.name
        if ( !json.containsKey( typeField ) )
        {
            throw IllegalArgumentException( "No '$typeField' defined." )
        }
        val typeJson = json[ typeField ].jsonObject.toString()
        type = Json.parse( DataType.serializer(), typeJson )
    }
}