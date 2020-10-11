package dk.cachet.carp.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * A uniquely named entity within a [namespace].
 * TODO: This likely needs more restrictions in line with other standards. Disallow certain symbols? Lower-case only?
 */
@Serializable( with = FullyQualifiedNameSerializer::class )
data class FullyQualifiedName(
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
         * Initializes a [FullyQualifiedName] based on a string, formatted as: "<namespace>.<name>".
         *
         * @throws IllegalArgumentException when no namespace is specified, i.e., [fullyQualifiedName] should contain at least one period.
         *   [name] will be set to the characters after the last period.
         */
        fun fromString( fullyQualifiedName: String ): FullyQualifiedName
        {
            val segments = fullyQualifiedName.split( '.' )
            require( segments.count() > 1 ) { "A namespace needs to be specified." }

            val namespace = segments.subList( 0, segments.size - 1 ).joinToString( "." )
            val name = segments.last()

            return FullyQualifiedName( namespace, name )
        }
    }
}


object FullyQualifiedNameSerializer : KSerializer<FullyQualifiedName>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("dk.cachet.carp.common.FullyQualifiedName", PrimitiveKind.STRING )


    override fun serialize( encoder: Encoder, value: FullyQualifiedName ) =
        encoder.encodeString( "${value.namespace}.${value.name}" )

    override fun deserialize( decoder: Decoder): FullyQualifiedName =
        FullyQualifiedName.fromString( decoder.decodeString() )
}
