package dk.cachet.carp.common

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor


/**
 * Represents a single moment in time, stored as milliseconds since 1 January 1970 UTC.
 */
@Serializable( DateTimeSerializer::class )
expect class DateTime( msSinceUTC: Long )
{
    val msSinceUTC: Long

    companion object
    {
        fun now(): DateTime
    }

    /**
     * Output as ISO 8601 UTC date and time in extended format with day and seconds precision with 3 decimal places following a period.
     * E.g., "2020-01-01T12:00:00.000Z"
     */
    override fun toString(): String
}


/**
 * A custom serializer for [DateTime].
 */
@Serializer( forClass = DateTime::class )
object DateTimeSerializer : KSerializer<DateTime>
{
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor( "dk.cachet.carp.common.DateTime", PrimitiveKind.STRING )

    override fun serialize( encoder: Encoder, value: DateTime ) = encoder.encodeLong( value.msSinceUTC )
    override fun deserialize( decoder: Decoder ): DateTime = DateTime( decoder.decodeLong() )
}
