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
 */
@Serializable( UUIDSerializer::class )
expect class UUID( stringRepresentation: String )
{
    val stringRepresentation: String

    companion object
    {
        fun randomUUID(): UUID
    }
}


/**
 * Regular expression to verify whether the string representation of a UUID is valid.
 */
val UUIDRegex = Regex( "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})" )


/**
 * A custom serializer for [UUID].
 */
object UUIDSerializer : KSerializer<UUID>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor( "dk.cachet.carp.common.UUID", PrimitiveKind.STRING )


    override fun serialize( encoder: Encoder, value: UUID )
    {
        encoder.encodeString( value.stringRepresentation )
    }

    override fun deserialize( decoder: Decoder ): UUID
    {
        return UUID( decoder.decodeString() )
    }
}
