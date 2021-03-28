package dk.cachet.carp.common.application

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter


private val FORMATTER = DateTimeFormatter.ISO_INSTANT


@Serializable( DateTimeSerializer::class )
actual data class DateTime actual constructor( actual val msSinceUTC: Long )
{
    private val dateTime = Instant.ofEpochMilli( msSinceUTC )


    actual companion object
    {
        actual fun now(): DateTime = DateTime( System.currentTimeMillis() )

        actual fun fromString( string: String ): DateTime
        {
            val parsed = FORMATTER.parse( string )
            val instant = Instant.from( parsed )
            return DateTime( instant.toEpochMilli() )
        }
    }


    actual override fun toString(): String = FORMATTER.format( dateTime )

    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is DateTime ) return false

        return msSinceUTC == other.msSinceUTC
    }

    override fun hashCode(): Int
    {
        return msSinceUTC.hashCode()
    }
}
