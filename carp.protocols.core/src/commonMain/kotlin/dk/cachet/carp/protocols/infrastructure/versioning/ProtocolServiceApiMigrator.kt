package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceInvoker
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import kotlinx.serialization.json.*


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration()
    {
        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.Add" ) {
                addVersionField( "protocol" )
            }
            ifType( "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.AddVersion" ) {
                addVersionField( "protocol" )
            }
        }

        override fun migrateResponse(
            request: JsonObject,
            response: ApiResponse,
            targetVersion: ApiVersion
        ): ApiResponse =
            when ( request.getType() )
            {
                // `StudyProtocolSnapshot` response.
                "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.UpdateParticipantDataConfiguration",
                "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.GetBy" ->
                {
                    val responseObject = (response.response as? JsonObject)?.migrate { removeVersionField() }
                    ApiResponse( responseObject, response.ex )
                }

                // `List<StudyProtocolSnapshot>` response.
                "dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest.GetAllForOwner" ->
                {
                    val responseArray = (response.response as? JsonArray)?.let { oldElements ->
                        val newElements = oldElements.map {
                            val jsonObject = requireNotNull( it as? JsonObject )
                            jsonObject.migrate { removeVersionField() }
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
    ProtocolServiceInvoker,
    ProtocolServiceRequest.Serializer,
    ProtocolService.Event.serializer(),
    listOf( major1Minor0To1Migration )
)
