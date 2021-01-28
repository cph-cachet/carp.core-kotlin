package dk.cachet.carp.studies.application

import dk.cachet.carp.common.ddd.SingleThreadedEventBus
import dk.cachet.carp.common.ddd.createApplicationServiceAdapter
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    override fun createService(): StudyService
    {
        val eventBus = SingleThreadedEventBus()
        val serviceBus = eventBus.createApplicationServiceAdapter( StudyService::class )

        return StudyServiceHost( InMemoryStudyRepository(), serviceBus )
    }
}
