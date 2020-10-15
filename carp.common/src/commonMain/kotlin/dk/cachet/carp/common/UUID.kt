package dk.cachet.carp.common

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor


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

    companion object
    {
        /**
         * Parse common [UUID] representations, also allowing upper case hexadecimal digits.
         *
         * TODO: It might be useful to allow even more flexible [uuid] entry (e.g., surrounded by curly braces, etc.).
         */
        fun parse( uuid: String ): UUID

        fun randomUUID(): UUID
    }
}


/**
 * Regular expression to match [UUID] using the recommended RFC 4122 notation. Hexadecimal digits should be lowercase.
 */
val UUIDRegex = Regex( "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})" )


/**
 * A custom serializer for [UUID].
 */
object UUIDSerializer : KSerializer<UUID>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor( "dk.cachet.carp.common.UUID", PrimitiveKind.STRING )


    override fun serialize( encoder: Encoder, value: UUID ) = encoder.encodeString( value.stringRepresentation )
    override fun deserialize( decoder: Decoder ): UUID = UUID( decoder.decodeString() )
}
