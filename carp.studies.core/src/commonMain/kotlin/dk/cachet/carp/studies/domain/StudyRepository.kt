package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner


interface StudyRepository
{
    /**
     * Add or update a [study] in the repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository on update
     * or if the study exists already on adding as new.
     */
    fun store( study: Study )

    /**
     * Returns the [Study] which has the specified [studyId], or null when no study is found.
     */
    fun getById( studyId: UUID ): Study?

    /**
     * Returns the studies created by the specified [owner].
     */
    fun getForOwner( owner: StudyOwner ): List<Study>

    /**
     * Adds a new [participant] for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist,
     * or when a participant with the specified ID already exists within the study.
     */
    fun addParticipant( studyId: UUID, participant: Participant )

    /**
     * Returns the participants which were added to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist.
     */
    fun getParticipants( studyId: UUID ): List<Participant>
}
