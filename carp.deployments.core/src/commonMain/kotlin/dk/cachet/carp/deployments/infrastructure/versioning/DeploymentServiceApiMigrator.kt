package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiMigration
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


private val addedSnapshotVersionField =
    object : ApiMigration( 0, 1 )
    {
        private val newField = "version"

        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest.CreateStudyDeployment" ) {
                updateObject( "protocol" ) { json[ newField ] = JsonPrimitive( 0 ) }
            }
        }

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) = response

        override fun migrateEvent( event: JsonObject ) = event.migrate {
            ifType( "dk.cachet.carp.deployments.application.DeploymentService.Event.StudyDeploymentCreated" ) {
                updateObject( "protocol" ) { json[ newField ] = JsonPrimitive( 0 ) }
            }
        }
    }


val DeploymentServiceApiMigrator = ApplicationServiceApiMigrator(
    DeploymentService.API_VERSION,
    DeploymentServiceRequest.Serializer,
    DeploymentService.Event.serializer(),
    listOf( addedSnapshotVersionField )
)
