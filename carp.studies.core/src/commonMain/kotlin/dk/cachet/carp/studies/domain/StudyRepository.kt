package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID


interface StudyRepository
{
    /**
     * Adds a new [study] to the repository.
     *
     * @throws IllegalArgumentException when a study with the same id already exists.
     */
    fun add( study: Study )

    /**
     * Returns the [Study] which has the specified [studyId], or null when no account is found.
     */
    fun getById( studyId: UUID ): Study?
}
