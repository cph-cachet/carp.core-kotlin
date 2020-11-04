package dk.cachet.carp.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * Creates a primitive serializer for CARP types in the [dk.cachet.carp.common] namespace
 * which are serialized using `toString` and deserialized using [fromString].
 */
internal inline fun <reified T> createCarpStringPrimitiveSerializer( noinline fromString: (String) -> T ) =
    object : StringConversionSerializer<T>( fullyQualified( T::class.simpleName!! ), fromString ) { }

internal open class StringConversionSerializer<T>(
    serialName: String,
    val fromString: (String) -> T
) : KSerializer<T>
{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor( serialName, PrimitiveKind.STRING )

    override fun serialize( encoder: Encoder, value: T ) = encoder.encodeString( value.toString() )
    override fun deserialize( decoder: Decoder ): T = fromString( decoder.decodeString() )
}


/**
 * Creates a primitive serializer for CARP types in the [dk.cachet.carp.common] namespace
 * which are serialized using [toLong] and deserialized using [fromLong].
 */
internal inline fun <reified T> createCarpLongPrimitiveSerializer(
    noinline fromLong: (Long) -> T,
    noinline toLong: (T) -> Long
) =
    object : LongConversionSerializer<T>( fullyQualified( T::class.simpleName!! ), fromLong, toLong ) { }

internal open class LongConversionSerializer<T>(
    serialName: String,
    val fromLong: (Long) -> T,
    val toLong: (T) -> Long
) : KSerializer<T>
{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor( serialName, PrimitiveKind.LONG )

    override fun serialize( encoder: Encoder, value: T ) = encoder.encodeLong( toLong( value ) )
    override fun deserialize( decoder: Decoder ): T = fromLong( decoder.decodeLong() )
}


private fun fullyQualified( typeName: String ) = "dk.cachet.carp.common.$typeName"
