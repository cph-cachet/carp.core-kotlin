package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID

/**
 * A [StudyRepository] which holds studies in memory as long as the instance is held in memory.
 */
class InMemoryStudyRepository : StudyRepository
{
    private val studies: MutableList<Study> = mutableListOf()


    override fun add( study: Study )
    {
        require( studies.none { it.id == study.id } )

        studies.add( study )
    }

    override fun getById( studyId: UUID ): Study? = studies.firstOrNull { it.id == studyId }
}


/**
 * Tests whether the [InMemoryUserRepository] stub is implemented correctly.
 */
class InMemoryStudyRepositoryTest : StudyRepositoryTest
{
    override fun createRepository(): StudyRepository = InMemoryStudyRepository()
}
