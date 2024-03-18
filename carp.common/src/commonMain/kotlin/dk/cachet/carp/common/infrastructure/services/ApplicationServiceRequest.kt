package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlin.js.JsExport
import kotlin.reflect.KCallable


/**
 * A request for [TService] stored in memory.
 */
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
abstract class ApplicationServiceRequest<TService : ApplicationService<TService, *>, out TReturn>
{
    @Required
    abstract val apiVersion: ApiVersion

    abstract fun getResponseSerializer(): KSerializer<out TReturn>

    /**
     * Determines whether this request object represents a call to the given service [request].
     *
     * The default implementation does a simple name comparison, so you may want to override this in case your
     * request objects differ from your application service method names, e.g., in the case of method overloads.
     */
    // This check could potentially be replaced with annotations on request classes which contain the method reference
    // if Kotlin ever makes those compile time constants: https://youtrack.jetbrains.com/issue/KT-16304
    open fun matchesServiceRequest( request: KCallable<*> ): Boolean
    {
        val requestObjectName = this::class.simpleName
        return request.name == requestObjectName?.replaceFirstChar { it.lowercase() }
    }
}
