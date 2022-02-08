package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.ApplicationServiceInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


/**
 * Use [json] to encode a [String] representation of this request, excluding the response.
 */
fun LoggedRequest<*>.encodeRequestToString( json: Json ): String
{
    val serviceInfo = ApplicationServiceInfo( serviceKlass.java )
    return json.encodeToString( serviceInfo.requestObjectSerializer, request )
}

/**
 * Use [json] to encode a [String] representation of the successful response of this request.
 */
fun LoggedRequest.Succeeded<*>.encodeResponseToString( json: Json ): String
{
    @Suppress( "UNCHECKED_CAST" )
    return json.encodeToString( request.getResponseSerializer() as KSerializer<Any?>, response )
}
