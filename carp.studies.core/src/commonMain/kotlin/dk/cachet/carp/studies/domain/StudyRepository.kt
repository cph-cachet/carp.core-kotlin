package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner


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
     * Adds a new [participant] for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist,
     * or when a participant with the specified ID already exists within the study.
     */
    suspend fun addParticipant( studyId: UUID, participant: Participant )

    /**
     * Returns the participants which were added to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist.
     */
    suspend fun getParticipants( studyId: UUID ): List<Participant>
}
