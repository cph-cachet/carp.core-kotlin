package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


/**
 * A class that represents an immutable universally unique identifier (UUID).
 * A UUID represents a 128-bit value.
 *
 * @param stringRepresentation The recommended RFC 4122 notation of UUID. Hexadecimal digits should be lowercase.
 */
@Serializable( UUIDSerializer::class )
expect class UUID( stringRepresentation: String )
{
    val stringRepresentation: String


    companion object : UUIDFactory
    {
        /**
         * Parse common [UUID] representations, also allowing upper case hexadecimal digits.
         *
         * TODO: It might be useful to allow even more flexible [uuid] entry (e.g., surrounded by curly braces, etc.).
         */
        fun parse( uuid: String ): UUID

        override fun randomUUID(): UUID
    }


    override fun toString(): String
}

interface UUIDFactory
{
    fun randomUUID(): UUID
}


/**
 * Regular expression to match [UUID] using the recommended RFC 4122 notation. Hexadecimal digits should be lowercase.
 */
val UUIDRegex = Regex( "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})" )


/**
 * A custom serializer for [UUID].
 */
object UUIDSerializer : KSerializer<UUID> by createCarpStringPrimitiveSerializer( { UUID( it ) } )
