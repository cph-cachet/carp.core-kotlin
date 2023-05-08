package dk.cachet.carp.common.application

import kotlin.js.JsExport


/**
 * A boolean value that can also be 'unknown' in case insufficient information is available.
 */
@JsExport
enum class Trilean
{
    TRUE,
    FALSE,
    UNKNOWN
}

/**
 * Convert a [Boolean] value into a corresponding [Trilean].
 */
@JsExport
fun Boolean.toTrilean(): Trilean
{
    return if ( this ) Trilean.TRUE else Trilean.FALSE
}
