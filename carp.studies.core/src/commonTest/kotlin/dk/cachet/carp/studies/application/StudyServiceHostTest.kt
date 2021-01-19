package dk.cachet.carp.studies.application

import dk.cachet.carp.common.ddd.ApplicationServiceEventBus
import dk.cachet.carp.common.ddd.SingleThreadedEventBus
import dk.cachet.carp.common.ddd.createApplicationServiceAdapter
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    override fun createService(): Pair<StudyService, ApplicationServiceEventBus<StudyService, StudyService.Event>>
    {
        val eventBus = SingleThreadedEventBus()
        val serviceBus = eventBus.createApplicationServiceAdapter( StudyService::class )

        val service = StudyServiceHost( InMemoryStudyRepository(), serviceBus )

        return Pair( service, serviceBus )
    }
}
