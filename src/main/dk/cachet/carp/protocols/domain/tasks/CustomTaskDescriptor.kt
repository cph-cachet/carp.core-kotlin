package dk.cachet.carp.protocols.domain.tasks

import com.beust.klaxon.*
import dk.cachet.carp.protocols.domain.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.serializer


/**
 * A wrapper used to load extending types from [TaskDescriptor]s serialized as JSON which are unknown at runtime.
 */
data class CustomTaskDescriptor( override val className: String, override val jsonSource: String )
    : TaskDescriptor(), UnknownPolymorphicWrapper
{
    override val name: String
    @Serializable( with = MeasuresSerializer::class )
    override val measures: List<Measure>

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        val nameField = TaskDescriptor::name.name
        name = json.string( nameField ) ?: throw IllegalArgumentException( "No '$nameField' defined." )

        // Get raw JSON string of measures (using klaxon) and use kotlinx serialization to deserialize.
        val measuresField = TaskDescriptor::measures.name
        val measuresJson = json.array<Measure>( measuresField )?.toJsonString() ?: throw IllegalArgumentException( "No '$measuresField' defined." )
        val serializer = ArrayListSerializer( Measure::class.serializer() )
        measures = JSON.parse( serializer, measuresJson )
    }
}