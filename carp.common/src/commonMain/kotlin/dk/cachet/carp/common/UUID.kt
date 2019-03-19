package dk.cachet.carp.common


/**
 * A class that represents an immutable universally unique identifier (UUID).
 * A UUID represents a 128-bit value.
 */
expect class UUID( stringRepresentation: String )
{
    companion object
    {
        fun randomUUID(): UUID
    }
}