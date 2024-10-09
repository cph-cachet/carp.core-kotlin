package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.*
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceInvoker
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration()
    {
        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest.CreateStudyDeployment" ) {
                addVersionField( "protocol" )
            }
        }

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) = response

        override fun migrateEvent( event: JsonObject ) = event.migrate {
            ifType( "dk.cachet.carp.deployments.application.DeploymentService.Event.StudyDeploymentCreated" ) {
                addVersionField( "protocol" )
            }
        }
    }

/**
 * - The `deviceDisplayName` field in `DeviceRegistration` is no longer required.
 *   This also apply to `ConnectedDeviceRegistration`.
 */
@Suppress( "MagicNumber" )
private val major1Minor1To3Migration =
    object : ApiMigration( 1, 3 )
    {
        private val removedField = "deviceDisplayName"

        @OptIn( ExperimentalSerializationApi::class )
        protected fun ApiJsonObjectMigrationBuilder.addDisplayNameField( fieldName: String )
        {
            updateObject( fieldName ) {
                json[ removedField ] ?: json.put( removedField, JsonPrimitive(null ) )
            }
        }

        override fun migrateRequest( request: JsonObject ) = request

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) =
            when ( request.getType( ) )
            {
                "dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest.GetDeviceDeploymentFor" ->
                {
                        val responseObject = (response.response as? JsonObject)?.migrate {
                            addDisplayNameField( "registration" )

                            val connectedDevices = json[ "connectedDeviceRegistrations" ]?.jsonObject

                            if ( connectedDevices != null )
                            {
                                updateObject( "connectedDeviceRegistrations" )
                                {
                                    for ( ( key ) in connectedDevices.entries )
                                    {
                                        addDisplayNameField( key )
                                    }
                                }
                            }
                        }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }
        override fun migrateEvent( event: JsonObject ) = event
    }

val DeploymentServiceApiMigrator = ApplicationServiceApiMigrator(
    DeploymentService.API_VERSION,
    DeploymentServiceInvoker,
    DeploymentServiceRequest.Serializer,
    DeploymentService.Event.serializer(),
    listOf( major1Minor0To1Migration, major1Minor1To3Migration )
)
