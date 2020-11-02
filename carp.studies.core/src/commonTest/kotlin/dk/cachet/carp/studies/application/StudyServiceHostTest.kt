package dk.cachet.carp.studies.application

import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    override fun createService(): StudyService = StudyServiceHost( InMemoryStudyRepository() )
}
