package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.services.ApplicationService
import java.lang.reflect.Method


/**
 * Example of a serialized request and [response] to an [applicationService] [method] using a [requestObject].
 */
class ExampleRequest<AS : ApplicationService<AS, *>>(
    val applicationService: Class<out ApplicationService<AS, *>>,
    val method: Method,
    val requestObject: JsonExample,
    val response: JsonExample
)
{
    data class JsonExample( val klass: Class<*>, val json: String )
}
