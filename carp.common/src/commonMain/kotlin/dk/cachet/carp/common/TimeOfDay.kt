package dk.cachet.carp.common

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor


/**
 * Represents a specific time of the day according to a 24-hour clock.
 */
@Serializable( TimeOfDaySerializer::class )
@Suppress( "MagicNumber" )
data class TimeOfDay( val hour: Int, val minutes: Int = 0, val seconds: Int = 0 )
{
    init
    {
        require(hour in 0..23 && minutes in 0..59 && seconds in 0..59 )
            { "The hour needs be between 0 and 23, and minutes and seconds between 0 and 59" }
    }

    /**
     * Output as ISO 8601 extended time format with seconds accuracy, omitting the 24th hour and 60th leap second.
     * E.g., "09:30:00".
     */
    override fun toString(): String
    {
        val hour = hour.toString().padStart( 2, '0' )
        val minutes = minutes.toString().padStart( 2, '0' )
        val seconds = seconds.toString().padStart( 2, '0' )

        return "$hour:$minutes:$seconds"
    }
}


/**
 * Regular expression to verify whether the string representation of a [TimeOfDay] is valid.
 */
val TimeOfDayRegex = Regex( """\d\d:\d\d:\d\d""" )


/**
 * A custom serializer for [TimeOfDay].
 */
object TimeOfDaySerializer : KSerializer<TimeOfDay>
{
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor( "dk.cachet.carp.common.TimeOfDay", PrimitiveKind.STRING )

    override fun serialize( encoder: Encoder, value: TimeOfDay ) =
        encoder.encodeString( value.toString() )

    override fun deserialize( decoder: Decoder ): TimeOfDay
    {
        val time = decoder.decodeString()
        require( TimeOfDayRegex.matches( time ) ) { "Invalid TimeOfDay string representation." }

        val (hour, minutes, seconds) = time.split( ':' )
        return TimeOfDay( hour.toInt(), minutes.toInt(), seconds.toInt() )
    }
}

