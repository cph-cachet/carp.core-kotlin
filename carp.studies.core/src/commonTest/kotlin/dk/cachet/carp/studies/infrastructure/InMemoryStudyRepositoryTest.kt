package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyRepositoryTest


/**
 * Tests whether [InMemoryStudyRepository] is implemented correctly.
 */
class InMemoryStudyRepositoryTest : StudyRepositoryTest
{
    override fun createRepository(): StudyRepository = InMemoryStudyRepository()
}
