package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner


interface StudyRepository
{
    /**
     * Adds a new [study] to the repository.
     *
     * @throws IllegalArgumentException when a study with the same id already exists.
     */
    fun add( study: Study )

    /**
     * Returns the [Study] which has the specified [studyId], or null when no study is found.
     */
    fun getById( studyId: UUID ): Study?

    /**
     * Returns the studies created by the specified [owner].
     */
    fun getForOwner( owner: StudyOwner ): List<Study>

    /**
     * Update a [study] which is already stored in this repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository.
     */
    fun update( study: Study )

    /**
     * Adds or removes participants for the study with [studyId] in the repository.
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist
     */
    fun updateParticipants( studyId: UUID, addParticipants: Set<Participant>, removeParticipants: Set<Participant> )

    /**
     * Returns the participants which were added to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist.
     */
    fun getParticipants( studyId: UUID ): List<Participant>

    /**
     * Update the study live status for study with id [studyId]
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist
     */
    fun updateLiveStatus( studyId: UUID, isLive: Boolean )

    /**
     * Update the study protocol for study with id [studyId]
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist
     */
    fun updateProtocol( studyId: UUID, protocol: StudyProtocolSnapshot )

    /**
     * Adds or removes participations in the study with id [studyId]
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist, or
     * if any added participations already exists.
     */
    fun updateParticipations(
        studyId: UUID,
        addParticipations: Set<DeanonymizedParticipation>,
        removeParticipations: Set<DeanonymizedParticipation>
    )
}
