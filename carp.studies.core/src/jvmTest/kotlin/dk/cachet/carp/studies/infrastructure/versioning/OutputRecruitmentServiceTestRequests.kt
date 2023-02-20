package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest
import dk.cachet.carp.studies.application.RecruitmentServiceTest
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator


class OutputRecruitmentServiceTestRequests :
    OutputTestRequests<RecruitmentService>( RecruitmentService::class ),
    RecruitmentServiceTest
{
    override fun createSUT(): RecruitmentServiceTest.SUT
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

        serviceLogger = logger

        return RecruitmentServiceTest.SUT( loggedService, sut.studyService, sut.eventBus )
    }
}
