package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiMigration
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


private val addedSnapshotVersionField =
    object : ApiMigration( 0, 1 )
    {
        private val newField = "version"

        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.SetProtocol" ) {
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
                "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.GetStudyDetails" ->
                {
                    val protocolField = "protocolSnapshot"
                    val responseObject = (response.response as? JsonObject)?.migrate {
                        if ( json[ protocolField ] != JsonNull )
                        {
                            updateObject( "protocolSnapshot" ) { json.remove( newField ) }
                        }
                    }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate {
            val protocolField = "protocolSnapshot"

            ifType( "dk.cachet.carp.studies.application.StudyService.Event.StudyCreated" ) {
                updateObject( "study" )
                {
                    if ( json[ protocolField ] != JsonNull )
                    {
                        updateObject( "protocolSnapshot" ) { json[ newField ] = JsonPrimitive( 0 ) }
                    }
                }
            }
            ifType( "dk.cachet.carp.studies.application.StudyService.Event.StudyGoneLive" ) {
                updateObject( "study" )
                {
                    if ( json[ protocolField ] != JsonNull )
                    {
                        updateObject( "protocolSnapshot" ) { json[ newField ] = JsonPrimitive( 0 ) }
                    }
                }
            }
        }
    }


val StudyServiceApiMigrator = ApplicationServiceApiMigrator(
    StudyService.API_VERSION,
    StudyServiceRequest.Serializer,
    StudyService.Event.serializer(),
    listOf( addedSnapshotVersionField )
)
