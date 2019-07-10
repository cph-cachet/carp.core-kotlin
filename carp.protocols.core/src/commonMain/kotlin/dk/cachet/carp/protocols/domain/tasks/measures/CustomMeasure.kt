package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [Measure] serialized as JSON which are unknown at runtime.
 */
data class CustomMeasure( override val className: String, override val jsonSource: String ) : Measure(), UnknownPolymorphicWrapper
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    override val type: DataType

    init
    {
        val json = JSON.parseJson( jsonSource ) as JsonObject

        // Get raw JSON string of type (using klaxon) and use kotlinx serialization to deserialize.
        val typeField = Measure::type.name
        if ( !json.containsKey( typeField ) )
        {
            throw IllegalArgumentException( "No '$typeField' defined." )
        }
        val typeJson = json[ typeField ]!!.jsonObject.toString()
        type = JSON.parse( DataType.serializer(), typeJson )
    }
}