package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


internal val API_VERSION_FIELD = ApplicationServiceRequest<*, *>::apiVersion.name

/**
 * Get the class discriminator for this JSON object; or null if no class discriminator is found.
 */
fun Map<String, JsonElement>.getType(): String?
{
    // TODO: In case `CLASS_DISCRIMINATOR` is ever changed, this can't be hardcoded.
    val type: JsonElement? = this[ CLASS_DISCRIMINATOR ]
    return if ( type is JsonPrimitive && type.isString ) type.content else null
}


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

    protected fun JsonObject.migrate( migration: ApiMigrationBuilder.() -> Unit ): JsonObject =
        ApiMigrationBuilder( this, minimumMinorVersion, targetMinorVersion )
            .apply( migration ).build()
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


class ApiMigrationBuilder(
    jsonObject: Map<String, JsonElement>,
    val minimumMinorVersion: Int,
    val targetMinorVersion: Int
)
{
    val json: MutableMap<String, JsonElement> = jsonObject.toMutableMap()

    init
    {
        // When API version is included, it always needs to be migrated.
        val version: JsonElement? = json[ API_VERSION_FIELD ]
        if ( version != null )
        {
            json[ API_VERSION_FIELD ] = version.replaceString( ".$minimumMinorVersion", ".$targetMinorVersion" )
        }
    }


    /**
     * Run a [migration] on this object in case it is a polymorphic type identified by [classDiscriminator].
     */
    fun ifType( classDiscriminator: String, migration: ApiMigrationBuilder.() -> Unit )
    {
        if ( json.getType() == classDiscriminator ) apply( migration )
    }

    /**
     * Retrieve the object identified by [fieldName] in this object, and run the specified [migration].
     */
    fun updateObject( fieldName: String, migration: ApiMigrationBuilder.() -> Unit )
    {
        val o = requireNotNull( json[ fieldName ] as? JsonObject )
        val newJson: JsonObject = ApiMigrationBuilder( o, minimumMinorVersion, targetMinorVersion )
            .apply( migration ).build()
        json[ fieldName ] = newJson
    }

    /**
     * Copy the value of the field with [fromFieldName] to the field with [toFieldName].
     */
    fun copyField( fromFieldName: String, toFieldName: String )
    {
        val from = requireNotNull( json[ fromFieldName ] )
        json[ toFieldName ] = from
    }

    private fun JsonElement.replaceString( oldValue: String, newValue: String ): JsonPrimitive
    {
        require( this is JsonPrimitive && this.isString )
        return JsonPrimitive( content.replace( oldValue, newValue ) )
    }

    fun build(): JsonObject = JsonObject( json.toMap() )
}
