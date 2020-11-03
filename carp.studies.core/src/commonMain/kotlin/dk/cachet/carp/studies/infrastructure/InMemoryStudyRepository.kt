package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.users.ParticipantRepository


/**
 * A [StudyRepository] which holds studies in memory as long as the instance is held in memory.
 */
class InMemoryStudyRepository : StudyRepository, ParticipantRepository by InMemoryParticipantRepository()
{
    private val studies: MutableMap<UUID, StudySnapshot> = mutableMapOf()


    /**
     * Adds a new [study] to the repository.
     *
     * @throws IllegalArgumentException when a study with the same id already exists.
     */
    override suspend fun add( study: Study )
    {
        require( !studies.contains( study.id ) )

        studies[ study.id ] = study.getSnapshot()
    }

    /**
     * Returns the [Study] which has the specified [studyId], or null when no study is found.
     */
    override suspend fun getById( studyId: UUID ): Study? = studies[ studyId ]?.let { Study.fromSnapshot( it ) }

    /**
     * Returns the studies created by the specified [owner].
     */
    override suspend fun getForOwner( owner: StudyOwner ): List<Study> =
        studies.values
            .filter { it.ownerId == owner.id }
            .map { Study.fromSnapshot( it ) }

    /**
     * Update a [study] which is already stored in this repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository.
     */
    override suspend fun update( study: Study )
    {
        require( studies.contains( study.id ) )

        studies[ study.id ] = study.getSnapshot()
    }
}
