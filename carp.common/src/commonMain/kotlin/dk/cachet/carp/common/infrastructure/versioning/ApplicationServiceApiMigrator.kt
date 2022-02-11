package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive


/**
 * Supports transforming between different API versions of [ApplicationServiceRequest] and [IntegrationEvent] objects.
 */
class ApplicationServiceApiMigrator<TService : ApplicationService<TService, *>>(
    val runtimeVersion: ApiVersion,
    val requestObjectSerializer: KSerializer<out ApplicationServiceRequest<TService, *>>
)
{
    companion object
    {
        /**
         * Retrieve the [ApiVersion] for the given [jsonObject].
         *
         * @throws IllegalArgumentException when [jsonObject] does not contain an [ApiVersion].
         */
        private fun getApiVersion( jsonObject: JsonObject ): ApiVersion
        {
            val requestVersionString = jsonObject[ "apiVersion" ]?.jsonPrimitive?.content
            requireNotNull( requestVersionString )
                { "Request object needs to contain `apiVersion` for migration to succeed." }

            return ApiVersion.fromString( requestVersionString )
        }
    }


    /**
     * Migrate the [request] so that it matches [runtimeVersion] and deserialize using [json].
     */
    fun migrateRequest( json: Json, request: JsonObject ): MigratedRequest<TService>
    {
        val requestVersion = getApiVersion( request )
        require( !requestVersion.isMoreRecent( runtimeVersion ) )
            { "Request version ($requestVersion) is more recent than the runtime version ($runtimeVersion)." }

        // Add migrations once needed.
        require( requestVersion == runtimeVersion )
            { "No migration for requests from $requestVersion to $runtimeVersion supported."}
        val downgradeResponse =
            {
                response: JsonElement?, ex: Exception? ->
                    if ( ex != null ) throw ex
                    else response!!
            }

        val updatedRequest = json.decodeFromJsonElement( requestObjectSerializer, request )
        return MigratedRequest( json, updatedRequest, downgradeResponse )
    }
}


/**
 * A [request] which can be invoked using [invokeOn] which will return the response expected by the version of the caller.
 */
class MigratedRequest<TService : ApplicationService<TService, *>>(
    val json: Json,
    val request: ApplicationServiceRequest<TService, *>,
    private val downgradeResponse: (JsonElement?, Exception?) -> JsonElement
)
{
    /**
     * Invoke this request on [service] and convert the response to the version expected by the original caller.
     */
    suspend fun invokeOn( service: TService ): JsonElement
    {
        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { request.invokeOn( service ) }
            catch ( ex: Exception ) { return downgradeResponse( null, ex ) }

        @Suppress( "UNCHECKED_CAST" )
        val responseJson = json.encodeToJsonElement( request.getResponseSerializer() as KSerializer<Any?>, response )
        return downgradeResponse( responseJson, null )
    }
}
