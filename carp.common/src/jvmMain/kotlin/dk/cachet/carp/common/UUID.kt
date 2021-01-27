package dk.cachet.carp.common

import kotlinx.serialization.Serializable


@Serializable( UUIDSerializer::class )
actual class UUID actual constructor( actual val stringRepresentation: String )
{
    init
    {
        require( UUIDRegex.matches( stringRepresentation ) ) { "Invalid UUID string representation." }
    }


    actual companion object
    {
        @Suppress( "ImplicitDefaultLocale" ) // Does not cause a bug now, and API will be updated in next Kotlin release: https://youtrack.jetbrains.com/issue/KT-43023
        actual fun parse( uuid: String ): UUID = UUID( uuid.toLowerCase() )
        actual fun randomUUID(): UUID = UUID( java.util.UUID.randomUUID().toString() )
    }


    actual override fun toString(): String = stringRepresentation

    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is UUID ) return false

        return stringRepresentation == other.stringRepresentation
    }

    override fun hashCode(): Int = stringRepresentation.hashCode()
}
