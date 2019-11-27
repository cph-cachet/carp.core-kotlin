package dk.cachet.carp.studies.application

import dk.cachet.carp.studies.domain.*


/**
 * Tests for [StudyServiceHost].
 */
class StudyServiceHostTest : StudyServiceTest
{
    override fun createStudyService(): Pair<StudyService, StudyRepository>
    {
        val repo = InMemoryStudyRepository()
        val service = StudyServiceHost( repo )

        return Pair( service, repo )
    }
}
