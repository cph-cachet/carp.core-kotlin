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
    val requestObjectSerializer: KSerializer<out ApplicationServiceRequest<TService, *>>,
    migrations: List<ApiMigration> = emptyList()
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
            val requestVersionString = jsonObject[ API_VERSION_FIELD ]?.jsonPrimitive?.content
            requireNotNull( requestVersionString )
                { "Request object needs to contain `apiVersion` for migration to succeed." }

            return ApiVersion.fromString( requestVersionString )
        }
    }


    // Sort migrations and throw exception if there are missing or conflicting migrations.
    private val migrations = migrations.sortedBy { it.minimumMinorVersion }.also {
        val targetVersion = runtimeVersion.minor

        val isRangeCovered =
            if ( it.isEmpty() ) targetVersion == 0
            else it.first().minimumMinorVersion == 0 && it.last().targetMinorVersion == targetVersion

        var curVersion = 0
        val noGapsOrConflicts = it.fold( true ) { isValid, migration ->
            val curValid = isValid && migration.minimumMinorVersion == curVersion
            curVersion = migration.targetMinorVersion
            curValid
        }

        require( isRangeCovered && noGapsOrConflicts ) { "There are missing or conflicting migrations." }
    }

    /**
     * Migrate the [request] so that it matches [runtimeVersion] and deserialize using [json].
     *
     * @throws IllegalArgumentException if:
     *  - the [request] version is more recent than the runtime version
     *  - the runtime version is a later major version than the [request] version
     */
    fun migrateRequest( json: Json, request: JsonObject ): MigratedRequest<TService>
    {
        val requestVersion = getApiVersion( request )
        require( !requestVersion.isMoreRecent( runtimeVersion ) )
            { "Request version ($requestVersion) is more recent than the runtime version ($runtimeVersion)." }
        require( runtimeVersion.major > requestVersion.major )
            { "Can't migrate to new major versions." }

        // Apply request migrations.
        val toApply = migrations.dropWhile { requestVersion.minor >= it.targetMinorVersion }
        val updatedRequest = toApply.fold( request ) { r, migration -> migration.migrateRequest( r ) }

        // TODO: apply response migrations.
        val downgradeResponse =
            {
                response: JsonElement?, ex: Exception? ->
                    if ( ex != null ) throw ex
                    else response!!
            }

        val decodedRequest = json.decodeFromJsonElement( requestObjectSerializer, updatedRequest )
        return MigratedRequest( json, decodedRequest, downgradeResponse )
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
