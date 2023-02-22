package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHostTest
import dk.cachet.carp.studies.application.StudyServiceTest
import dk.cachet.carp.studies.infrastructure.StudyServiceDecorator
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest


class OutputStudyServiceTestRequests :
    OutputTestRequests<StudyService, StudyService.Event, StudyServiceRequest<*>>(
        StudyService::class,
        ::StudyServiceDecorator
    ),
    StudyServiceTest
{
    override fun createService(): StudyService
    {
        val (service, eventBus) = StudyServiceHostTest.createService()
        return createLoggedApplicationService( service, eventBus )
    }
}
