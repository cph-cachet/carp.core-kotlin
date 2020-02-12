package dk.cachet.carp.studies.application

import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import dk.cachet.carp.studies.domain.StudyRepository


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    override fun createService(): Pair<StudyService, StudyRepository>
    {
        val repo = InMemoryStudyRepository()
        val service = StudyServiceHost( repo )

        return Pair( service, repo )
    }
}
