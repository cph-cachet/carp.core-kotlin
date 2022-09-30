package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import kotlinx.serialization.json.JsonObject


/**
 * An [ApiMigration] which does not require any changes to JSON, other than updating the API version.
 */
class UnchangedMigration( minimumMinorVersion: Int, targetMinorVersion: Int ) :
    ApiMigration( minimumMinorVersion, targetMinorVersion )
{
    override fun migrateRequest( request: JsonObject ) = request.migrate { }
    override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) = response
    override fun migrateEvent( event: JsonObject ) = event.migrate { }
}
