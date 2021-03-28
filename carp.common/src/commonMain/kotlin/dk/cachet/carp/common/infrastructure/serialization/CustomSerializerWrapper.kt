package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor


/**
 * A wrapper for objects which need to be serialized using a different serializer than the one configured at compile time.
 * When the [Encoder] encounters this wrapper, [inner] will be serialized using [serializer].
 */
@Serializable( CustomSerializerWrapperSerializer::class )
class CustomSerializerWrapper internal constructor( val inner: Any, val serializer: KSerializer<Any> )


object CustomSerializerWrapperSerializer : KSerializer<CustomSerializerWrapper>
{
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor( "dk.cachet.carp.common.infrastructure.serialization.CustomSerializerWrapper" )

    override fun serialize( encoder: Encoder, value: CustomSerializerWrapper ) =
        encoder.encodeSerializableValue( value.serializer, value.inner )

    override fun deserialize( decoder: Decoder ): CustomSerializerWrapper =
        throw UnsupportedOperationException( "${CustomSerializerWrapper::class.simpleName} only supports serialization." )
}


/**
 * Specify a custom [serializer] to use for [inner].
 * When the [Encoder] encounters this wrapper, [inner] will be serialized using [serializer] instead.
 */
@Suppress( "UNCHECKED_CAST" )
fun <T : Any> customSerializerWrapper( inner: T, serializer: KSerializer<T> ): CustomSerializerWrapper
{
    return CustomSerializerWrapper( inner, serializer as KSerializer<Any> )
}
