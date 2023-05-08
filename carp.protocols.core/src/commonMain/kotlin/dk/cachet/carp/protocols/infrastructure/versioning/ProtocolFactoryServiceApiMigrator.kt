package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceInvoker
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import kotlinx.serialization.json.*


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration()
    {
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
                    val responseObject = (response.response as? JsonObject)?.migrate { removeVersionField() }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate { }
    }


val ProtocolFactoryServiceApiMigrator = ApplicationServiceApiMigrator(
    ProtocolFactoryService.API_VERSION,
    ProtocolFactoryServiceInvoker,
    ProtocolFactoryServiceRequest.Serializer,
    ProtocolFactoryService.Event.serializer(),
    listOf( major1Minor0To1Migration )
)
