package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceMock


/**
 * Tests for [RecruitmentServiceRequest]'s.
 */
class ParticipantServiceRequestsTest : ApplicationServiceRequestsTest<RecruitmentService, RecruitmentServiceRequest>(
    RecruitmentService::class,
    RecruitmentServiceMock(),
    RecruitmentServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<RecruitmentServiceRequest> = listOf(
            RecruitmentServiceRequest.AddParticipant( studyId, EmailAddress( "test@test.com" ) ),
            RecruitmentServiceRequest.GetParticipant( studyId, UUID.randomUUID() ),
            RecruitmentServiceRequest.GetParticipants( studyId ),
            RecruitmentServiceRequest.DeployParticipantGroup( studyId, setOf() ),
            RecruitmentServiceRequest.GetParticipantGroupStatusList( studyId ),
            RecruitmentServiceRequest.StopParticipantGroup( studyId, UUID.randomUUID() ),
            RecruitmentServiceRequest.SetParticipantGroupData( studyId, UUID.randomUUID(), InputDataType( "some", "type" ), CustomInput( "Test" ) )
        )
    }
}
