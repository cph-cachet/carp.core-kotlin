package dk.cachet.carp.common

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Serializable( DateTimeSerializer::class )
actual class DateTime actual constructor( actual val msSinceUTC: Long )
{
    actual companion object
    {
        actual fun now(): DateTime
        {
            return DateTime( System.currentTimeMillis() )
        }
    }


    private val dateTime = Instant.ofEpochMilli( msSinceUTC )

    actual override fun toString(): String =
        DateTimeFormatter
            .ofPattern( "YYYY-mm-dd'T'HH:mm:ss.SSS'Z'" )
            .withZone( ZoneId.of( "UTC" ) )
            .format( dateTime )

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
