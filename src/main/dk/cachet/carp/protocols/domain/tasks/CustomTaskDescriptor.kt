package dk.cachet.carp.protocols.domain.tasks

import com.beust.klaxon.*
import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicWrapper
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.internal.ReferenceArraySerializer
import kotlinx.serialization.json.JSON


/**
 * A wrapper used to load extending types from [TaskDescriptor]s serialized as JSON which are unknown at runtime.
 */
data class CustomTaskDescriptor( override val className: String, override val jsonSource: String )
    : TaskDescriptor(), UnknownPolymorphicWrapper
{
    override val name: String
    override val measures: Iterable<Measure>

    init
    {
        val json = Parser().parse( StringBuilder( jsonSource ) ) as JsonObject

        val nameField = TaskDescriptor::name.name
        name = json.string( nameField ) ?: throw IllegalArgumentException( "No '$nameField' defined." )

        // Get raw JSON string of measures (using klaxon) and use kotlinx serialization to deserialize.
        val measuresField = TaskDescriptor::measures.name
        val measuresJson = json.array<Measure>( measuresField )?.toJsonString() ?: throw IllegalArgumentException( "No '$measuresField' defined." )
        val serializer = ReferenceArraySerializer( Any::class, PolymorphicSerializer )
        measures = JSON.parse( serializer, measuresJson ).map { m -> m as Measure }
    }
}