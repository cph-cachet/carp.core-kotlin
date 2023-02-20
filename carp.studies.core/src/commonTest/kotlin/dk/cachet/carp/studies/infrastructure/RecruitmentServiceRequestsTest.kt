package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest
import dk.cachet.carp.studies.application.StudyService


/**
 * Tests for [RecruitmentServiceRequest]'s.
 */
class RecruitmentServiceRequestsTest : ApplicationServiceRequestsTest<RecruitmentService, RecruitmentServiceRequest<*>>(
    RecruitmentServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<RecruitmentServiceRequest<*>> = listOf(
            RecruitmentServiceRequest.AddParticipant( studyId, EmailAddress( "test@test.com" ) ),
            RecruitmentServiceRequest.GetParticipant( studyId, UUID.randomUUID() ),
            RecruitmentServiceRequest.GetParticipants( studyId ),
            RecruitmentServiceRequest.InviteNewParticipantGroup( studyId, setOf() ),
            RecruitmentServiceRequest.GetParticipantGroupStatusList( studyId ),
            RecruitmentServiceRequest.StopParticipantGroup( studyId, UUID.randomUUID() )
        )
    }


    override fun createServiceLoggingProxy(): ApplicationServiceLogger<RecruitmentService, *>
    {
        val sut = RecruitmentServiceHostTest.createSUT()

        val (loggedService, logger) = createLoggedApplicationService(
            sut.recruitmentService,
            ::RecruitmentServiceDecorator,
            EventBusLog(
                sut.eventBus,
                EventBusLog.Subscription( RecruitmentService::class, RecruitmentService.Event::class ),
                EventBusLog.Subscription( StudyService::class, StudyService.Event::class )
            )
        )

        // TODO: The base class relies on the proxied service also be a logger.
        return object :
            ApplicationServiceLogger<RecruitmentService, RecruitmentService.Event> by logger,
            RecruitmentService by loggedService { }
    }
}
