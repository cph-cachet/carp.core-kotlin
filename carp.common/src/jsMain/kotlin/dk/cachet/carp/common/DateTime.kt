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
}
