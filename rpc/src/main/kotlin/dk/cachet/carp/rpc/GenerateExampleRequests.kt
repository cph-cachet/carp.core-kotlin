@file:Suppress( "WildcardImport" )

package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.*
import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.data.application.*
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
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
        @Suppress( "UNCHECKED_CAST" )
        val responseSerializer =
            (example.overrideResponseSerializer ?: request.returnType.kotlin.serializer()) as KSerializer<Any>
        val responseJson = json.encodeToString( responseSerializer, example.response )

        ExampleRequest(
            applicationServiceInterface,
            request,
            ExampleRequest.JsonExample( requestObject, requestObjectJson ),
            ExampleRequest.JsonExample( request.returnType, responseJson )
        )
    }
}


private class Example( val request: Any, val response: Any, val overrideResponseSerializer: KSerializer<*>? = null )

private val exampleRequests: Map<KFunction<*>, Example> = mapOf(
    // TODO: Add an example for each request.
    DataStreamService::getDataStream to Example(
        request = DataStreamServiceRequest.GetDataStream( DataStreamId( UUID.randomUUID(), "Test", DataType( "namespace", "name" ) ), 0 ),
        response = MutableDataStreamBatch(),
        overrideResponseSerializer = DataStreamBatchSerializer
    )
)
