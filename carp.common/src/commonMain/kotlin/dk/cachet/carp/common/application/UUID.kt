package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.*


/**
 * A class that represents an immutable universally unique identifier (UUID).
 * A UUID represents a 128-bit value.
 *
 * @param stringRepresentation The recommended RFC 4122 notation of UUID. Hexadecimal digits should be lowercase.
 */
@Serializable( UUIDSerializer::class )
class UUID( val stringRepresentation: String )
{
    init
    {
        require( UUIDRegex.matches( stringRepresentation ) ) { "Invalid UUID string representation." }
    }

    companion object
    {
        /**
         * Parse common [UUID] representations, also allowing upper case hexadecimal digits.
         *
         * TODO: It might be useful to allow even more flexible [uuid] entry (e.g., surrounded by curly braces, etc.).
         */
        fun parse( uuid: String ): UUID = UUID( uuid.lowercase() )

        fun randomUUID(): UUID = DefaultUUIDFactory.randomUUID()
    }


    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is UUID ) return false

        return stringRepresentation == other.stringRepresentation
    }

    override fun hashCode(): Int = stringRepresentation.hashCode()

    override fun toString(): String = stringRepresentation
}

/**
 * Regular expression to match [UUID] using the recommended RFC 4122 notation. Hexadecimal digits should be lowercase.
 */
val UUIDRegex = Regex( "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})" )

/**
 * A custom serializer for [UUID].
 */
object UUIDSerializer : KSerializer<UUID> by createCarpStringPrimitiveSerializer( { UUID( it ) } )


expect object DefaultUUIDFactory : UUIDFactory
{
    override fun randomUUID(): UUID
}

interface UUIDFactory
{
    fun randomUUID(): UUID
}
