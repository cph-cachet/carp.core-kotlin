package dk.cachet.carp.common

import java.time.LocalDateTime


actual class DateTime( private val dateTime: LocalDateTime )
{
    actual companion object {
        actual fun now(): DateTime {
            return DateTime(LocalDateTime.now())
        }
    }
}