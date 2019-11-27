package dk.cachet.carp.common


/**
 * A boolean value that can also be 'unknown' in case insufficient information is available.
 */
enum class Trilean
{
    TRUE,
    FALSE,
    UNKNOWN
}

/**
 * Convert a [Boolean] value into a corresponding [Trilean].
 */
fun Boolean.toTrilean(): Trilean
{
    return if ( this ) Trilean.TRUE else Trilean.FALSE
}
