package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import dk.cachet.carp.common.infrastructure.versioning.getType
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceInvoker
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration()
    {
        override fun migrateRequest( request: JsonObject ) = request.migrate { }

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) =
            when ( request.getType() )
            {
                "dk.cachet.carp.data.infrastructure.DataStreamServiceRequest.GetDataStream" ->
                {
                    val responseObject = (response.response as? JsonArray)?.migrate {
                        objects {
                            updateArray( "measurements" ) {
                                objects {
                                    updateObject( "data" ) {
                                        val isCarpType = json.getType() in CarpDataTypes.keys.map { it.toString() }
                                        if ( isCarpType ) json.remove( "sensorSpecificData" )
                                    }
                                }
                            }
                        }
                    }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate { }
    }


val DataStreamServiceApiMigrator = ApplicationServiceApiMigrator(
    DataStreamService.API_VERSION,
    DataStreamServiceInvoker,
    DataStreamServiceRequest.Serializer,
    DataStreamService.Event.serializer(),
    listOf( major1Minor0To1Migration )
)
