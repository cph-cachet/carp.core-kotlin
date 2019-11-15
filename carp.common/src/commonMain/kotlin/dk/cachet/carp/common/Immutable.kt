package dk.cachet.carp.common

import kotlinx.serialization.*


/**
 * A base class which verifies whether the derived implementation is immutable during initialization.
 * TODO: Since reflection is not yet available for JavaScript runtimes, this is only verified by the JVM runtime.
 *
 * Immutable types may not contain mutable properties and may only contain data classes and basic types.
 *
 * @param exception The exception to throw in case the implementation is not immutable. [NotImmutableError] should be thrown by default.
 */
@Serializable
expect abstract class Immutable
{
    constructor( exception: Throwable = NotImmutableError() )
}


/**
 * Exception which is thrown by default when an extending class of [Immutable] is not implemented as immutable.
 */
class NotImmutableError(
    error: String = "Immutable types should be data classes, may not contain mutable properties, and may only contain basic types and other Immutable properties." ) : Throwable( error )