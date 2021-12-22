@file:JsExport

package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * A uniquely named identifier within a [namespace].
 */
@Serializable( with = NamespacedIdSerializer::class )
data class NamespacedId(
    /**
     * Uniquely identifies the organization/person who determines how to interpret [name].
     * The expected format is lowercase alphanumeric (and underscores) words delimited by dots.
     * To prevent conflicts, a reverse domain namespace is suggested.
     *
     * Example: "tld.domain.subdomain", "org.openmhealth", or "dk.cachet.carp".
     */
    val namespace: String,
    /**
     * Uniquely identifies something within the [namespace].
     * The expected format is a single lowercase alphanumeric (and underscores) word.
     * The name may not contain any periods. Periods are reserved for namespaces.
     *
     * Example: "myname" or "my_name".
     */
    val name: String
)
{
    init
    {
        require( namespaceRegex.matches( namespace ) )
            { "Invalid namespace representation: expected lowercase alpha-numeric (underscore included) words delimited by dots." }
        require( nameRegex.matches( name ) )
            { "Invalid name representation: expected a single lowercase alpha-numeric (underscore included) word." }
    }


    companion object
    {
        /**
         * Initializes a [NamespacedId] based on a string, formatted as: "<namespace>.<name>".
         * It is fairly lenient and will lowercase and trim the input.
         *
         * @throws IllegalArgumentException when no namespace is specified, i.e., [fullyQualifiedName] should contain at least one period.
         *   [name] will be set to the characters after the last period.
         */
        fun fromString( fullyQualifiedName: String ): NamespacedId
        {
            val segments = fullyQualifiedName.trim().lowercase().split( '.' )
            require( segments.count() > 1 ) { "A namespace needs to be specified." }

            val namespace = segments.subList( 0, segments.size - 1 ).joinToString( "." )
            val name = segments.last()

            return NamespacedId( namespace, name )
        }
    }


    override fun toString(): String = "$namespace.$name"
}

/**
 * Regular expression to match a valid [NamespacedId.namespace] format.
 * It must consist of one or more lowercase words, using alpha-numerics and underscores, delimited by dots.
 * The namespace cannot end with a dot.
 */
val namespaceRegex = """^([a-z_0-9]+\.?)+[a-z_0-9]$""".toRegex()

/**
 * Regular expression to match a valid [NamespacedId.name] format.
 * It must be a single lowercase word consisting only of alpha-numerics and underscores.
 */
val nameRegex = """^[a-z_0-9]+?$""".toRegex()

/**
 * A custom serializer for [NamespacedId].
 */
object NamespacedIdSerializer : KSerializer<NamespacedId> by createCarpStringPrimitiveSerializer( { s -> NamespacedId.fromString( s ) } )
