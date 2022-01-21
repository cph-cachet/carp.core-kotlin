package dk.cachet.carp.rpc

import java.lang.reflect.Method


/**
 * Example of a serialized request and [response] to a [method] using a [requestObject].
 */
class ExampleRequest( val method: Method, val requestObject: JsonExample, val response: JsonExample )
{
    data class JsonExample( val klass: Class<*>, val json: String )
}
