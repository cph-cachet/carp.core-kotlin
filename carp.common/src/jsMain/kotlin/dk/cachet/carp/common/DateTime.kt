package dk.cachet.carp.common

import kotlin.js.Date


actual class DateTime( private val dateTime: Date )
{
    actual companion object
    {
        actual fun now(): DateTime
        {
            return DateTime( Date( Date.now() ) )
        }
    }

    actual override fun toString(): String
    {
        val isoString = dateTime.toISOString()
        val msAndTimeZoneLength = 5 // E.g., ".000Z"
        val dropMs = isoString.dropLast( msAndTimeZoneLength )
        return dropMs + "Z"
    }
}
