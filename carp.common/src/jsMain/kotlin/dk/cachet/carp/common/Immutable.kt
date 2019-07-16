package dk.cachet.carp.common

import kotlinx.serialization.Serializable


/**
 * TODO: Since reflection is not yet available for JavaScript runtimes, this implementation does nothing.
 */
@Serializable
actual abstract class Immutable
{
    actual constructor( exception: Throwable )
}