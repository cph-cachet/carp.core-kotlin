package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiMigration
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import kotlinx.serialization.json.JsonObject


private val addedSnapshotVersionField =
    object : ApiMigration( 0, 1 )
    {
        private val newField = "version"

        override fun migrateRequest( request: JsonObject ) = request.migrate { }

        override fun migrateResponse(
            request: JsonObject,
            response: ApiResponse,
            targetVersion: ApiVersion
        ): ApiResponse =
            when ( request.getType() )
            {
                "dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest.CreateCustomProtocol" ->
                {
                    val responseObject = (response.response as? JsonObject)?.migrate { json.remove( newField ) }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate { }
    }


val ProtocolFactoryServiceApiMigrator = ApplicationServiceApiMigrator(
    ProtocolFactoryService.API_VERSION,
    ProtocolFactoryServiceRequest.Serializer,
    ProtocolFactoryService.Event.serializer(),
    listOf( addedSnapshotVersionField )
)
