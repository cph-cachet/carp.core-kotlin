package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiMigration
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


private val addedSnapshotVersionField =
    object : ApiMigration( 0, 1 )
    {
        private val newField = "version"

        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.Add" ) {
                updateObject( "protocol" ) { json[ newField ] = JsonPrimitive( 0 ) }
            }
            ifType( "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.AddVersion" ) {
                updateObject( "protocol" ) { json[ newField ] = JsonPrimitive( 0 ) }
            }
        }

        override fun migrateResponse(
            request: JsonObject,
            response: ApiResponse,
            targetVersion: ApiVersion
        ): ApiResponse =
            when ( request.getType() )
            {
                // Remove new field from `StudyProtocolSnapshot`.
                "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.UpdateParticipantDataConfiguration",
                "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.GetBy" ->
                {
                    val responseObject = (response.response as? JsonObject)?.migrate { json.remove( newField ) }
                    ApiResponse( responseObject, response.ex )
                }

                // Remove new field from each element in `List<StudyProtocolSnapshot>`.
                "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.GetAllForOwner" ->
                {
                    val responseArray = (response.response as? JsonArray)?.let { oldElements ->
                        val newElements = oldElements.map {
                            val jsonObject = requireNotNull( it as? JsonObject )
                            jsonObject.migrate { json.remove( newField ) }
                        }
                        JsonArray( newElements )
                    }
                    ApiResponse( responseArray, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate { }
    }


val ProtocolServiceApiMigrator = ApplicationServiceApiMigrator(
    ProtocolService.API_VERSION,
    ProtocolServiceRequest.Serializer,
    ProtocolService.Event.serializer(),
    listOf(addedSnapshotVersionField)
)
