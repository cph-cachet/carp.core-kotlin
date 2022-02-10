package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest
import dk.cachet.carp.studies.application.RecruitmentServiceTest
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceLoggingProxy


class OutputRecruitmentServiceTestRequests :
    OutputTestRequests<RecruitmentService>( RecruitmentService::class ),
    RecruitmentServiceTest
{
    override fun createService(): RecruitmentServiceTest.DependentServices
    {
        val services = RecruitmentServiceHostTest.createService()
        val service = RecruitmentServiceLoggingProxy( services.recruitmentService, services.eventBus )
        loggedService = service

        return RecruitmentServiceTest.DependentServices(
            service,
            services.studyService,
            services.eventBus
        )
    }
}
