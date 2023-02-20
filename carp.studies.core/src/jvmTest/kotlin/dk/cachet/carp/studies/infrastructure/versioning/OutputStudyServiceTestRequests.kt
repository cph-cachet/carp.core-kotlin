package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.createLoggedApplicationService
import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHostTest
import dk.cachet.carp.studies.application.StudyServiceTest
import dk.cachet.carp.studies.infrastructure.StudyServiceDecorator


class OutputStudyServiceTestRequests :
    OutputTestRequests<StudyService>( StudyService::class ),
    StudyServiceTest
{
    override fun createService(): StudyService
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

        serviceLogger = logger

        return loggedService
    }
}
