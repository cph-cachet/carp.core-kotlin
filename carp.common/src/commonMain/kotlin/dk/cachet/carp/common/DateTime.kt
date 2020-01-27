package dk.cachet.carp.common

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer


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
    override fun serialize( encoder: Encoder, obj: DateTime )
    {
        encoder.encodeLong( obj.msSinceUTC )
    }

    override fun deserialize( decoder: Decoder ): DateTime
    {
        return DateTime( decoder.decodeLong() )
    }
}
