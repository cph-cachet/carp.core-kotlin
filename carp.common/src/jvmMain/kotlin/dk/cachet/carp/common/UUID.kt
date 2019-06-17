package dk.cachet.carp.common


actual class UUID actual constructor( actual val stringRepresentation: String )
{
    actual companion object
    {
        actual fun randomUUID(): UUID
        {
            return UUID( java.util.UUID.randomUUID().toString() )
        }
    }


    override fun toString(): String
    {
        return stringRepresentation
    }

    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is UUID ) return false

        return stringRepresentation == other.stringRepresentation
    }

    override fun hashCode(): Int
    {
        return stringRepresentation.hashCode()
    }
}