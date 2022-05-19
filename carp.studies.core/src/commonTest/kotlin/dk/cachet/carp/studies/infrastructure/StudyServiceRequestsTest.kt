package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHostTest


/**
 * Tests for [StudyServiceRequest]'s.
 */
class StudyServiceRequestsTest : ApplicationServiceRequestsTest<StudyService, StudyServiceRequest<*>>(
    StudyServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<StudyServiceRequest<*>> = listOf(
            StudyServiceRequest.CreateStudy( UUID.randomUUID(), "Test", "Description", StudyInvitation( "Some study" ) ),
            StudyServiceRequest.SetInternalDescription( studyId, "New name", "New description" ),
            StudyServiceRequest.GetStudyDetails( studyId ),
            StudyServiceRequest.GetStudyStatus( studyId ),
            StudyServiceRequest.GetStudiesOverview( UUID.randomUUID() ),
            StudyServiceRequest.SetInvitation( studyId, StudyInvitation( "Some study" ) ),
            StudyServiceRequest.SetProtocol( studyId, StudyProtocol( UUID.randomUUID(), "Test" ).getSnapshot() ),
            StudyServiceRequest.RemoveProtocol( studyId ),
            StudyServiceRequest.GoLive( studyId ),
            StudyServiceRequest.Remove( studyId )
        )
    }


    override fun createServiceLoggingProxy(): ApplicationServiceLoggingProxy<StudyService, StudyService.Event> =
        StudyServiceHostTest.createService().let { StudyServiceLoggingProxy( it.first, it.second ) }
}
