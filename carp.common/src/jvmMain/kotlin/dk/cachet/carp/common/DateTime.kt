package dk.cachet.carp.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


actual class DateTime( private val dateTime: LocalDateTime )
{
    actual companion object
    {
        actual fun now(): DateTime
        {
            return DateTime( LocalDateTime.now() )
        }
    }

    actual override fun toString(): String
    {
        val isoFormatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss'Z'" )
        return dateTime.format( isoFormatter )
    }
}
