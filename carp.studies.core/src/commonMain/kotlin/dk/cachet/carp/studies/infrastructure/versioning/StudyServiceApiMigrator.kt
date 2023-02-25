package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.infrastructure.StudyServiceInvoker
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import kotlinx.serialization.json.*


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration()
    {
        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.SetProtocol" ) {
                addVersionField( "protocol" )
            }
        }

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) =
            when ( request.getType() )
            {
                // Remove new field from `StudyProtocolSnapshot`.
                "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.GetStudyDetails" ->
                {
                    val responseObject = (response.response as? JsonObject)
                        ?.migrate { removeVersionField( "protocolSnapshot" ) }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate {
            ifType( "dk.cachet.carp.studies.application.StudyService.Event.StudyCreated" ) {
                updateObject( "study" )
                {
                    val protocolField = "protocolSnapshot"
                    if ( json[ protocolField ] != JsonNull ) addVersionField( protocolField )
                }
            }
            ifType( "dk.cachet.carp.studies.application.StudyService.Event.StudyGoneLive" ) {
                updateObject( "study" )
                {
                    val protocolField = "protocolSnapshot"
                    if ( json[ protocolField ] != JsonNull ) addVersionField( protocolField )
                }
            }
        }
    }


val StudyServiceApiMigrator = ApplicationServiceApiMigrator(
    StudyService.API_VERSION,
    StudyServiceInvoker,
    StudyServiceRequest.Serializer,
    StudyService.Event.serializer(),
    listOf( major1Minor0To1Migration )
)
