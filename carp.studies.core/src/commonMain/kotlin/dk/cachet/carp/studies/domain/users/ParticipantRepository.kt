package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID


/**
 * Store [Participant] instances, linked to study IDs.
 */
interface ParticipantRepository
{
    /**
     * Adds a new [participant] for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a participant with the specified ID already exists within the study.
     */
    suspend fun addParticipant( studyId: UUID, participant: Participant )

    /**
     * Remove all data (participants) for the study with [studyId].
     *
     * @return True when all data for the study was removed; false when no data for the study is present in the repository.
     */
    suspend fun removeStudy( studyId: UUID ): Boolean

    /**
     * Returns the participants which were added to the study with the specified [studyId].
     */
    suspend fun getParticipants( studyId: UUID ): List<Participant>
}
