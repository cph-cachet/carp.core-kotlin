package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * A [StudyRepository] which holds studies in memory as long as the instance is held in memory.
 */
class InMemoryStudyRepository : StudyRepository
{
    private val studies: MutableMap<UUID, StudySnapshot> = mutableMapOf()
    private val participants: MutableMap<UUID, MutableList<Participant>> = mutableMapOf()

    /**
     * Returns the [Study] which has the specified [studyId], or null when no study is found.
     */
    override fun getById( studyId: UUID ): Study? = studies[ studyId ]?.let { Study.fromSnapshot( it ) }

    /**
     * Returns the studies created by the specified [owner].
     */
    override fun getForOwner( owner: StudyOwner ): List<Study> =
        studies.values
            .filter { it.ownerId == owner.id }
            .map { Study.fromSnapshot( it ) }

    /**
     * Add or update a [study] in the repository.
     */
    override fun store( study: Study )
    {
        studies[ study.id ] = study.getSnapshot()
    }

    /**
     * Adds a new [participant] for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist,
     * or when a participant with the specified ID already exists within the study.
     */
    override fun addParticipant( studyId: UUID, participant: Participant )
    {
        require( studies.contains( studyId ) )

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
        require( studies.contains( studyId ) )

        return participants[ studyId ] ?: listOf()
    }
}
