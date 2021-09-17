package dk.cachet.carp.common.application.services

import kotlin.reflect.KCallable


/**
 * An argument passed to the application service [endpoint] is invalid.
 *
 * When the application service is exposed as a web service, this maps to response status code 400 (Bad Request).
 */
class ApplicationServiceArgumentException(
    val endpoint: KCallable<*>,
    cause: Throwable? = null,
    message: String? = null
) : IllegalArgumentException( "Illegal argument passed to \"${endpoint.name}\": ${message ?: cause?.message}", cause )


/**
 * The requested operation exposed by [endpoint] is invalid given the current state of the objects it acts on.
 *
 * When this application service is exposed as a web service, this maps to response status code 409 (Conflict).
 */
class ApplicationServiceStateException(
    val endpoint: KCallable<*>,
    cause: Throwable? = null,
    message: String? = null
) : IllegalArgumentException( "Illegal state while calling \"${endpoint.name}\": ${message ?: cause?.message}", cause )


/**
 * Catches [IllegalArgumentException]s that are thrown from [block] and
 * wraps them as [ApplicationServiceArgumentException] with the additional [endpoint] information.
 */
suspend fun <TReturn> wrapArgumentExceptions( endpoint: KCallable<*>, block: suspend () -> TReturn ): TReturn =
    try { block() }
    catch ( ex: IllegalArgumentException ) { throw ApplicationServiceArgumentException( endpoint, ex ) }

/**
 * Catches [IllegalStateException]s that are thrown from [block] and
 * wraps them as [ApplicationServiceStateException] with the additional [endpoint] information.
 */
suspend fun <TReturn> wrapStateExceptions( endpoint: KCallable<*>, block: suspend () -> TReturn ): TReturn =
    try { block() }
    catch ( ex: IllegalStateException ) { throw ApplicationServiceStateException( endpoint, ex ) }

/**
 * Catches [IllegalArgumentException]s and [IllegalStateException]s that are thrown from [block] and
 * wraps them in the corresponding application service exceptions with the additional [endpoint] information.
 */
suspend fun <TReturn> wrapExceptions( endpoint: KCallable<*>, block: suspend() -> TReturn ): TReturn =
    try { block() }
    catch ( ex: IllegalArgumentException ) { throw ApplicationServiceArgumentException( endpoint, ex ) }
    catch ( ex: IllegalStateException ) { throw ApplicationServiceStateException( endpoint, ex ) }
