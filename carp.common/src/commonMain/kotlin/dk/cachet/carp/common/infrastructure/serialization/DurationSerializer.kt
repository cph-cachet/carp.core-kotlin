package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration


/**
 * Serializes [Duration] by converting it to microseconds.
 */
object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor( "$NAMESPACE.${Duration::class.simpleName!!}", PrimitiveKind.STRING )

    override fun deserialize( decoder: Decoder ): Duration = Duration.parseIsoString( decoder.decodeString() )
    override fun serialize( encoder: Encoder, value: Duration ) = encoder.encodeString( value.toIsoString() )
}
