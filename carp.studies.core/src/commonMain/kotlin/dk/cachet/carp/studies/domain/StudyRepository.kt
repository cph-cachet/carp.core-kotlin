package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.repository.RepositorySubCollection
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner


interface StudyRepository
{
    val participants: RepositorySubCollection<UUID, Participant>
    val participations: RepositorySubCollection<UUID, DeanonymizedParticipation>

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
}
