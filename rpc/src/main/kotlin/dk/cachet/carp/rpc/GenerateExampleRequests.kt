@file:Suppress( "WildcardImport" )

package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.*
import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.data.application.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction


/**
 * Generate a single [ExampleRequest] using the corresponding request object for each of the methods in
 * application service [AS].
 */
@OptIn( InternalSerializationApi::class )
fun <AS : ApplicationService<AS, *>> generateExampleRequests(
    applicationServiceInterface: Class<out ApplicationService<AS, *>>,
    requestObjectSuperType: Class<*>
): List<ExampleRequest<AS>>
{
    val requests = applicationServiceInterface.methods
    val requestObjects = requestObjectSuperType.classes

    val json = Json( createDefaultJSON() ) { prettyPrint = true }
    @Suppress( "UNCHECKED_CAST" )
    val requestObjectSerializer = requestObjectSuperType.kotlin.serializer() as KSerializer<Any>

    return requests.map { request ->
        val requestName = applicationServiceInterface.name + "." + request.name
        val requestObjectName = request.name.replaceFirstChar { it.uppercase() }
        val requestObject = requestObjects.singleOrNull { it.simpleName == requestObjectName }
        checkNotNull( requestObject )
            {
                "Could not find request object for $requestName. " +
                "Searched for: ${requestObjectSuperType.name}.$requestObjectName"
            }

        // Retrieve example and verify whether it is valid.
        val example = checkNotNull( exampleRequests[ request.kotlinFunction ] )
            { "No example request and response instances provided for $requestName." }
        check( requestObject.isInstance( example.request ) )
            { "Incorrect request instance provided for $requestName." }
        check( request.returnType.isInstance( example.response ) )
            { "Incorrect response instance provided for $requestName." }

        // Create example JSON request and response.
        val requestObjectJson = json.encodeToString( requestObjectSerializer, example.request )
        val responseJson = json.encodeToString( example.getResponseSerializer( request ), example.response )

        ExampleRequest(
            applicationServiceInterface,
            request,
            ExampleRequest.JsonExample( requestObject, requestObjectJson ),
            ExampleRequest.JsonExample( request.returnType, responseJson )
        )
    }
}


private class Example(
    val request: Any,
    val response: Any = Unit,
    val overrideResponseSerializer: KSerializer<*>? = null
)
{
    @Suppress( "UNCHECKED_CAST" )
    fun getResponseSerializer( request: Method ): KSerializer<Any> = getSerializer( request ) as KSerializer<Any>

    @OptIn( InternalSerializationApi::class )
    private fun getSerializer( request: Method ): KSerializer<*>
    {
        if ( overrideResponseSerializer != null ) return overrideResponseSerializer

        val returnType = request.kotlinFunction!!.returnType
        return serializer( returnType )
    }
}

private val exampleRequests: Map<KFunction<*>, Example> = mapOf(
)
