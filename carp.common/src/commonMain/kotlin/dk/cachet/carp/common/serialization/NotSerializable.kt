package dk.cachet.carp.common.serialization

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException


/**
 * A dummy serializer which can be applied to types that are never expected to be serialized,
 * but for which the compiler is trying to generate/retrieve a serializer. E.g., types used as generic type parameters.
 * Applying `@Serializable( with = NotSerializable::class )` to those types ensures compilation succeeds,
 * without having to actually make them serializable.
 */
object NotSerializable : KSerializer<Any>
{
    private val exception = SerializationException(
        "Types annotated as `@Serializable( with = NotSerializable::class )` are never expected to be serialized. " +
        "The serializer is only defined since the compiler does not know this, causing a compilation error." )

    override val descriptor: SerialDescriptor = SerialDescriptor( "This should never be serialized." )
    override fun deserialize( decoder: Decoder ): Any = throw exception
    override fun serialize( encoder: Encoder, value: Any ) = throw exception
}
