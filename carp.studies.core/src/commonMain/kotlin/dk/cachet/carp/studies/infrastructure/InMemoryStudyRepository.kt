package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository


/**
 * A [StudyRepository] which holds studies in memory as long as the instance is held in memory.
 */
class InMemoryStudyRepository : StudyRepository
{
    private val studies: MutableList<Study> = mutableListOf()


    /**
     * Adds a new [study] to the repository.
     *
     * @throws IllegalArgumentException when a study with the same id already exists.
     */
    override fun add( study: Study )
    {
        require( studies.none { it.id == study.id } )

        studies.add( study )
    }

    /**
     * Returns the [Study] which has the specified [studyId], or null when no study is found.
     */
    override fun getById( studyId: UUID ): Study? = studies.firstOrNull { it.id == studyId }
}
