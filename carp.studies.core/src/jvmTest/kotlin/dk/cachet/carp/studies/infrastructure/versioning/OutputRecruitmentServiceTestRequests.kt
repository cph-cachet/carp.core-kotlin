package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest
import dk.cachet.carp.studies.application.RecruitmentServiceTest
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceLog


private val services = RecruitmentServiceHostTest.createService()

class OutputProtocolServiceTestRequests :
    OutputTestRequests<RecruitmentService>(
        RecruitmentService::class,
        RecruitmentServiceLog( services.first, services.third )
    ),
    RecruitmentServiceTest
{
    override fun createService(): Pair<RecruitmentService, StudyService> = Pair( loggedService, services.second )
}
