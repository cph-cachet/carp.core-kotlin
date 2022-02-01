package dk.cachet.carp.studies.application

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
        fun createService(): StudyService
        {
            val eventBus = SingleThreadedEventBus()
            val serviceBus = eventBus.createApplicationServiceAdapter( StudyService::class )

            return StudyServiceHost( InMemoryStudyRepository(), serviceBus )
        }
    }

    override fun createService(): StudyService = StudyServiceHostTest.createService()
}
