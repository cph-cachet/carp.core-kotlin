package dk.cachet.carp.common

import kotlinx.serialization.Serializable
import kotlin.js.Date


@Serializable( DateTimeSerializer::class )
actual data class DateTime actual constructor( actual val msSinceUTC: Long )
{
    private val dateTime: Date = Date( msSinceUTC )


    actual companion object
    {
        actual fun now(): DateTime = DateTime( Date.now().toLong() )
        actual fun fromString( string: String ): DateTime
        {
            val date = Date.parse( string )
            return DateTime( date.toLong() )
        }
    }


    actual override fun toString(): String = dateTime.toISOString()

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
