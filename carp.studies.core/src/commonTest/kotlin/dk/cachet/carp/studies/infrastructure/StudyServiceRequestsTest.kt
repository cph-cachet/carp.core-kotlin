package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
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


    override fun createServiceLoggingProxy(): ApplicationServiceLogger<StudyService, *>
    {
        val (service, eventBus) = StudyServiceHostTest.createService()

        val (loggedService, logger) = createLoggedApplicationService(
            service,
            ::StudyServiceDecorator,
            EventBusLog(
                eventBus,
                EventBusLog.Subscription( StudyService::class, StudyService.Event::class )
            )
        )

        // TODO: The base class relies on the proxied service also be a logger.
        return object :
            ApplicationServiceLogger<StudyService, StudyService.Event> by logger,
            StudyService by loggedService { }
    }
}
