package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHost


/**
 * Tests for [StudyServiceRequest]'s.
 */
class StudyServiceRequestsTest : ApplicationServiceRequestsTest<StudyService, StudyServiceRequest>(
    StudyService::class,
    StudyServiceRequest.serializer(),
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<StudyServiceRequest> = listOf(
            StudyServiceRequest.CreateStudy( UUID.randomUUID(), "Test", "Description", StudyInvitation( "Some study" ) ),
            StudyServiceRequest.SetInternalDescription( studyId, "New name", "New description" ),
            StudyServiceRequest.GetStudyDetails( studyId ),
            StudyServiceRequest.GetStudyStatus( studyId ),
            StudyServiceRequest.GetStudiesOverview( UUID.randomUUID() ),
            StudyServiceRequest.SetInvitation( studyId, StudyInvitation( "Some study" ) ),
            StudyServiceRequest.SetProtocol( studyId, StudyProtocol( UUID.randomUUID(), "Test" ).getSnapshot() ),
            StudyServiceRequest.GoLive( studyId ),
            StudyServiceRequest.Remove( studyId )
        )
    }


    override fun createServiceLog(): ApplicationServiceLog<StudyService> = StudyServiceLog(
        StudyServiceHost(
            InMemoryStudyRepository(),
            SingleThreadedEventBus().createApplicationServiceAdapter( StudyService::class )
        )
    )
}
