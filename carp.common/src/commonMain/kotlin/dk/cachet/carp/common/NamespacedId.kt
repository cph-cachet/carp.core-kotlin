package dk.cachet.carp.common

import dk.cachet.carp.common.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


/**
 * A uniquely named identifier within a [namespace].
 * TODO: This likely needs more restrictions in line with other standards. Disallow certain symbols? Lower-case only?
 */
@Serializable( with = NamespacedIdSerializer::class )
data class NamespacedId(
    /**
     * Uniquely identifies the organization/person who determines how to interpret [name].
     * To prevent conflicts, a reverse domain namespace is suggested: e.g., "org.openmhealth" or "dk.cachet.carp".
     */
    val namespace: String,
    /**
     * Uniquely identifies something within the [namespace].
     *
     * The name may not contain any periods. Periods are reserved for namespaces.
     */
    val name: String
)
{
    init
    {
        require( namespace.isNotEmpty() ) { "Namespace needs to be set." }
        require( !name.contains( '.' ) ) { "Name may not contain any periods. Periods are reserved for namespaces." }
    }


    companion object
    {
        /**
         * Initializes a [NamespacedId] based on a string, formatted as: "<namespace>.<name>".
         *
         * @throws IllegalArgumentException when no namespace is specified, i.e., [fullyQualifiedName] should contain at least one period.
         *   [name] will be set to the characters after the last period.
         */
        fun fromString( fullyQualifiedName: String ): NamespacedId
        {
            val segments = fullyQualifiedName.split( '.' )
            require( segments.count() > 1 ) { "A namespace needs to be specified." }

            val namespace = segments.subList( 0, segments.size - 1 ).joinToString( "." )
            val name = segments.last()

            return NamespacedId( namespace, name )
        }
    }


    override fun toString(): String = "$namespace.$name"
}


/**
 * A custom serializer for [NamespacedId].
 */
object NamespacedIdSerializer : KSerializer<NamespacedId> by createCarpStringPrimitiveSerializer( { s -> NamespacedId.fromString( s ) } )
