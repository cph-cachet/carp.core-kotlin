package dk.cachet.carp.protocols.domain.tasks

import com.beust.klaxon.*
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.json.JSON


/**
 * A wrapper used to load extending types from [Measure]s serialized as JSON which are unknown at runtime.
 */
data class CustomMeasure( override val className: String, override val jsonSource: String ) : Measure(), UnknownPolymorphicWrapper
{
    override val type: DataType

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        // Get raw JSON string of type (using klaxon) and use kotlinx serialization to deserialize.
        val typeField = Measure::type.name
        val typeJson = json.array<Any>( typeField )?.toJsonString() ?: throw IllegalArgumentException( "No '$typeField' defined." )
        type = JSON.parse( DataTypeSerializer, typeJson )
    }
}