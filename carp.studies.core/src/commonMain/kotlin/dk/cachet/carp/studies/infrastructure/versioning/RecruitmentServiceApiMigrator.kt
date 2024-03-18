package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiMigration
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceInvoker
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import kotlinx.serialization.json.JsonObject


const val recruitmentRequest = "dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest"

private val major1Minor0To2Migration =
   object : ApiMigration( 0, 2 )
   {
       override fun migrateRequest( request: JsonObject ): JsonObject = request.migrate {
           ifType( "$recruitmentRequest.AddParticipant" )
           {
               changeType( "$recruitmentRequest.AddParticipantByEmailAddress" )
           }
       }

       override fun migrateResponse(
           request: JsonObject,
           response: ApiResponse,
           targetVersion: ApiVersion
       ): ApiResponse = response

       override fun migrateEvent( event: JsonObject ): JsonObject = event
   }

val RecruitmentServiceApiMigrator = ApplicationServiceApiMigrator(
    RecruitmentService.API_VERSION,
    RecruitmentServiceInvoker,
    RecruitmentServiceRequest.Serializer,
    RecruitmentService.Event.serializer(),
    listOf( major1Minor0To2Migration )
)
