package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


internal val API_VERSION_FIELD = ApplicationServiceRequest<*, *>::apiVersion.name


/**
 * Provides a conversion for request objects, responses, and integration events of API versions starting from
 * [minimumMinorVersion] to be migrated to [targetMinorVersion].
 */
abstract class ApiMigration( val minimumMinorVersion: Int, val targetMinorVersion: Int )
{
    init
    {
        require( minimumMinorVersion >= 0 && targetMinorVersion >= 0 ) { "API minor version must be positive." }
        require( targetMinorVersion > minimumMinorVersion )
            { "Version being migrated to needs to be newer than old version." }
    }

    abstract fun migrateRequest( request: JsonObject ): JsonObject
    abstract fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ): ApiResponse
    abstract fun migrateEvent( event: JsonObject ): JsonObject

    protected fun JsonElement.replaceString( oldValue: String, newValue: String ): JsonPrimitive
    {
        require( this is JsonPrimitive && this.isString )
        return JsonPrimitive( content.replace( oldValue, newValue ) )
    }
}


/**
 * The [response] to an API request, if successful, [ex] otherwise.
 */
class ApiResponse( val response: JsonElement?, val ex: Exception? )
{
    init
    {
        require( (response != null && ex == null) || (ex != null && response == null) )
            { "Response or exception needs to be set, but not both." }
    }
}


/**
 * An [ApiMigration] which does not require any changes to JSON, other than updating the API version.
 */
class UnchangedMigration( minimumMinorVersion: Int, targetMinorVersion: Int ) :
    ApiMigration( minimumMinorVersion, targetMinorVersion )
{
    override fun migrateRequest( request: JsonObject ): JsonObject = updateVersion( request )
    override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ): ApiResponse =
        response
    override fun migrateEvent( event: JsonObject ): JsonObject = updateVersion( event )

    private fun updateVersion( toUpdate: JsonObject ) = toUpdate
        .map {
            if ( it.key == API_VERSION_FIELD )
            {
                it.key to it.value.replaceString( ".$minimumMinorVersion", ".$targetMinorVersion" )
            }
            else it.key to it.value
        }
        .let { fields -> JsonObject( fields.toMap() ) }
}
