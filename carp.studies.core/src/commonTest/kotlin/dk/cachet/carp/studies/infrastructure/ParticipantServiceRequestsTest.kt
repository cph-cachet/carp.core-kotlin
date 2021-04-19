package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.studies.application.ParticipantService
import dk.cachet.carp.studies.application.ParticipantServiceMock


/**
 * Tests for [ParticipantServiceRequest]'s.
 */
class ParticipantServiceRequestsTest : ApplicationServiceRequestsTest<ParticipantService, ParticipantServiceRequest>(
    ParticipantService::class,
    ParticipantServiceMock(),
    ParticipantServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<ParticipantServiceRequest> = listOf(
            ParticipantServiceRequest.AddParticipant( studyId, EmailAddress( "test@test.com" ) ),
            ParticipantServiceRequest.GetParticipant( studyId, UUID.randomUUID() ),
            ParticipantServiceRequest.GetParticipants( studyId ),
            ParticipantServiceRequest.DeployParticipantGroup( studyId, setOf() ),
            ParticipantServiceRequest.GetParticipantGroupStatusList( studyId ),
            ParticipantServiceRequest.StopParticipantGroup( studyId, UUID.randomUUID() ),
            ParticipantServiceRequest.SetParticipantGroupData( studyId, UUID.randomUUID(), InputDataType( "some", "type" ), CustomInput( "Test" ) )
        )
    }
}
