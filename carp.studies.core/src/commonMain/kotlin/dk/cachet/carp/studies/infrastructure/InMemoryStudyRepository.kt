package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.users.Participant


/**
 * A [StudyRepository] which holds studies in memory as long as the instance is held in memory.
 */
class InMemoryStudyRepository : StudyRepository
{
    private val studies: MutableList<Study> = mutableListOf()
    private val participants: MutableMap<UUID, MutableList<Participant>> = mutableMapOf()


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

    /**
     * Returns the studies created by the specified [owner].
     */
    override fun getForOwner( owner: StudyOwner ): List<Study> =
        studies.filter { it.owner.id == owner.id }

    /**
     * Adds a new [participant] for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist,
     * or when a participant with the specified ID already exists within the study.
     */
    override fun addParticipant( studyId: UUID, participant: Participant )
    {
        require( studies.any { it.id == studyId } )

        val studyParticipants = participants.getOrPut( studyId ) { mutableListOf() }
        require( studyParticipants.none { it.id == participant.id } )
        studyParticipants.add( participant )
    }

    /**
     * Returns the participants which were added to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist.
     */
    override fun getParticipants( studyId: UUID ): List<Participant>
    {
        require( studies.any { it.id == studyId } )

        return participants[ studyId ] ?: listOf()
    }
}
