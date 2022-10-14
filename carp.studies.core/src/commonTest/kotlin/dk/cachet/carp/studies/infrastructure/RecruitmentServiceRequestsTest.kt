package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest


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


    override fun createServiceLoggingProxy(): ApplicationServiceLoggingProxy<RecruitmentService, RecruitmentService.Event> =
        RecruitmentServiceHostTest
            .createSUT()
            .let { RecruitmentServiceLoggingProxy( it.recruitmentService, it.eventBus ) }
}
