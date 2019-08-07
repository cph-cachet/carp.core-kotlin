package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.data.*
import kotlinx.serialization.json.*


/**
 * A wrapper used to load extending types from [Measure] serialized as JSON which are unknown at runtime.
 */
data class CustomMeasure( override val className: String, override val jsonSource: String, val serializer: Json )
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