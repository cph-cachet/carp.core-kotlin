package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.Recruitment
import dk.cachet.carp.studies.domain.RecruitmentSnapshot
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.ParticipantRepository


class InMemoryParticipantRepository : ParticipantRepository
{
    private val participants: MutableMap<UUID, MutableList<Participant>> = mutableMapOf()
    private val recruitments: MutableMap<UUID, RecruitmentSnapshot> = mutableMapOf()


    /**
     * Adds a new [participant] for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a participant with the specified ID already exists within the study.
     */
    override suspend fun addParticipant( studyId: UUID, participant: Participant )
    {
        val studyParticipants = participants.getOrPut( studyId ) { mutableListOf() }
        require( studyParticipants.none { it.id == participant.id } )
        studyParticipants.add( participant )
    }

    /**
     * Returns the participants which were added to the study with the specified [studyId].
     */
    override suspend fun getParticipants( studyId: UUID ): List<Participant> = participants[ studyId ] ?: listOf()

    /**
     * Add a new [Recruitment] to the repository.
     *
     * @throws IllegalArgumentException when a recruitment with the same studyId already exists.
     */
    override suspend fun addRecruitment( recruitment: Recruitment )
    {
        require( recruitment.studyId !in recruitments )
        recruitments[ recruitment.studyId ] = recruitment.getSnapshot()
    }

    /**
     * Returns the [Recruitment] for the specified [studyId], or null when no recruitment is found.
     */
    override suspend fun getRecruitment( studyId: UUID ): Recruitment? =
        recruitments[ studyId ]?.let { Recruitment.fromSnapshot( it ) }

    /**
     * Update a [Recruitment] which is already stored in this repository.
     *
     * @throws IllegalArgumentException when no previous version of this recruitment is stored in the repository.
     */
    override suspend fun updateRecruitment( recruitment: Recruitment )
    {
        require( recruitment.studyId in recruitments )
        recruitments[ recruitment.studyId ] = recruitment.getSnapshot()
    }

    /**
     * Remove all data (participants and recruitment) for the study with [studyId].
     *
     * @return True when all data for the study was removed; false when no data for the study is present in the repository.
     */
    override suspend fun removeStudy( studyId: UUID ): Boolean
    {
        val areParticipantsRemoved = participants.remove( studyId ) != null
        val isRecruitmentRemoved = recruitments.remove( studyId ) != null
        return areParticipantsRemoved || isRecruitmentRemoved
    }
}
