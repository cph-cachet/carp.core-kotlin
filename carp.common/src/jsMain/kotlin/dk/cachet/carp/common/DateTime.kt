package dk.cachet.carp.common

import kotlinx.serialization.Serializable
import kotlin.js.Date


@Serializable( DateTimeSerializer::class )
actual class DateTime actual constructor( actual val msSinceUTC: Long )
{
    actual companion object
    {
        actual fun now(): DateTime
        {
            return DateTime( Date.now().toLong() )
        }
    }


    private val dateTime: Date = Date( msSinceUTC )

    actual fun defaultFormat(): String = dateTime.toISOString()

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
