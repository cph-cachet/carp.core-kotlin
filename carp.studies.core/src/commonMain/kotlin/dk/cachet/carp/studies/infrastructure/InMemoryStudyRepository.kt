package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant


/**
 * A [StudyRepository] which holds studies in memory as long as the instance is held in memory.
 */
class InMemoryStudyRepository : StudyRepository
{
    private val studies: MutableMap<UUID, StudySnapshot> = mutableMapOf()
    private val participants: MutableMap<UUID, MutableList<Participant>> = mutableMapOf()


    /**
     * Adds a new [study] to the repository.
     *
     * @throws IllegalArgumentException when a study with the same id already exists.
     */
    override fun add( study: Study )
    {
        require( !studies.contains( study.id ) )

        studies[ study.id ] = study.getSnapshot()
    }

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
     * Update a [study] which is already stored in this repository.
     *
     * @throws IllegalArgumentException when no previous version of this study is stored in the repository.
     */
    override fun update( study: Study )
    {
        require( studies.contains( study.id ) )

        studies[ study.id ] = study.getSnapshot()
    }

    /**
     * Adds or removes participants for the study with [studyId] to the repository.
     *
     * @throws IllegalArgumentException when a study with the specified [studyId] does not exist,
     * or when a participant with the specified ID already exists within the study.
     */
    override fun updateParticipants(
        studyId: UUID,
        addParticipants: Set<Participant>,
        removeParticipants: Set<Participant>
    )
    {
        require( studies.contains( studyId ) )

        val studyParticipants = participants.getOrPut( studyId ) { mutableListOf() }

        require( addParticipants.intersect(studyParticipants).isEmpty() )

        participants[studyId] = studyParticipants
            .union( addParticipants )
            .minus( removeParticipants )
            .toMutableList()
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

    override fun updateLiveStatus( studyId: UUID, isLive: Boolean )
    {
        require( studies.contains( studyId ) )

        studies[ studyId ] = studies[ studyId ]!!.copy(isLive = isLive)
    }

    override fun updateProtocol( studyId: UUID, protocol: StudyProtocolSnapshot )
    {
        require( studies.contains( studyId ) )

        studies[ studyId ] = studies[ studyId ]!!.copy(protocolSnapshot = protocol)
    }

    override fun updateParticipations(
        studyId: UUID,
        addParticipations: Set<DeanonymizedParticipation>,
        removeParticipations: Set<DeanonymizedParticipation>
    )
    {
        require( studies.contains( studyId ) )

        val updatedParticipations = studies[ studyId ]!!.participations
            .union( addParticipations )
            .minus( removeParticipations )

        studies[ studyId ] = studies[ studyId ]!!.copy( participations = updatedParticipations )
    }
}
