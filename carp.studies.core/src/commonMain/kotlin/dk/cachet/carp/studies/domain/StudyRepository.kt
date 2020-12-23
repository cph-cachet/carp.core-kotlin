package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Store [Study] instances.
 */
interface StudyRepository
{
    /**
     * Adds a new [study] to the repository.
     *
     * @throws IllegalArgumentException when a study with the same id already exists.
     */
    suspend fun add( study: Study )

    /**
     * Returns the [Study] which has the specified [studyId], or null when no study is found.
     */
    suspend fun getById( studyId: UUID ): Study?

    /**
     * Returns the studies created by the specified [owner].
     */
    suspend fun getForOwner( owner: StudyOwner ): List<Study>

    /**
     * Update a [study] which is already stored in this repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository.
     */
    suspend fun update( study: Study )

    /**
     * Remove the [Study] with the specified [studyId] from the repository.
     *
     * @return True when the study was removed; false when the study is not present in the repository.
     */
    suspend fun remove( studyId: UUID ): Boolean
}
