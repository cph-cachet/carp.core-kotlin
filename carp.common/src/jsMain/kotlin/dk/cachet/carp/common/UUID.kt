package dk.cachet.carp.common


actual class UUID actual constructor( actual val stringRepresentation: String )
{
    init
    {
        require( UUIDRegex.matches( stringRepresentation ) ) { "Invalid UUID string representation." }
    }

    actual companion object
    {
        actual fun randomUUID(): UUID
        {
            // We do not need to support UUID generation in JS, as this is client-side code.
            // Furthermore, it does not seem like it can generate true UUIDs:
            // https://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
            throw UnsupportedOperationException( "JavaScript runtimes cannot generate true UUIDs." )
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