package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    companion object
    {
        fun createService(): Pair<StudyService, EventBus>
        {
            val eventBus = SingleThreadedEventBus()
            val studyService = StudyServiceHost(
                InMemoryStudyRepository(),
                eventBus.createApplicationServiceAdapter( StudyService::class )
            )

            return Pair( studyService, eventBus )
        }
    }

    override fun createService(): StudyService = StudyServiceHostTest.createService().first
}
