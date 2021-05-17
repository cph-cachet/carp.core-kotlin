package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceMock
import dk.cachet.carp.studies.application.users.StudyOwner


/**
 * Tests for [StudyServiceRequest]'s.
 */
class StudyServiceRequestsTest : ApplicationServiceRequestsTest<StudyService, StudyServiceRequest>(
    StudyService::class,
    StudyServiceMock(),
    StudyServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<StudyServiceRequest> = listOf(
            StudyServiceRequest.CreateStudy( StudyOwner(), "Test", "Description", StudyInvitation.empty() ),
            StudyServiceRequest.SetInternalDescription( studyId, "New name", "New description" ),
            StudyServiceRequest.GetStudyDetails( studyId ),
            StudyServiceRequest.GetStudyStatus( studyId ),
            StudyServiceRequest.GetStudiesOverview( StudyOwner() ),
            StudyServiceRequest.SetInvitation( studyId, StudyInvitation.empty() ),
            StudyServiceRequest.SetProtocol( studyId, StudyProtocol( ProtocolOwner(), "Test" ).getSnapshot() ),
            StudyServiceRequest.GoLive( studyId ),
            StudyServiceRequest.Remove( studyId )
        )
    }
}
