package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant


/**
 * Represents a set of participants recruited for a [Study] identified by [studyId].
 */
class Recruitment( val studyId: UUID ) :
    AggregateRoot<Recruitment, RecruitmentSnapshot, Recruitment.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class ParticipationAdded( val studyDeploymentId: UUID, val participation: DeanonymizedParticipation ) : Event()
    }


    companion object
    {
        fun fromSnapshot( snapshot: RecruitmentSnapshot ): Recruitment
        {
            val recruitment = Recruitment( snapshot.studyId )
            recruitment.creationDate = snapshot.creationDate
            if ( snapshot.studyProtocol != null && snapshot.invitation != null )
            {
                recruitment.readyForDeployment( snapshot.studyProtocol, snapshot.invitation )
            }
            for ( p in snapshot.participations )
            {
                recruitment._participations[ p.key ] = p.value.toMutableSet()
            }

            return recruitment
        }
    }


    /**
     * The snapshot of the study protocol to which participants in this recruitment can be invited.
     */
    var studyProtocol: StudyProtocolSnapshot? = null
        private set

    /**
     * The invitation to send to invited participants.
     */
    var invitation: StudyInvitation? = null
        private set
    val canAddParticipations: Boolean get() = studyProtocol != null && invitation != null

    /**
     * Lock in the [protocol] which participants in this recruitment can participate in,
     * and the [invitation] which is sent to them once they are deployed.
     */
    fun readyForDeployment( protocol: StudyProtocolSnapshot, invitation: StudyInvitation )
    {
        this.studyProtocol = protocol
        this.invitation = invitation
    }

    /**
     * Per study deployment ID, the set of participants that participate in it.
     * TODO: Maybe this should be kept private and be replaced with clearer helper functions (e.g., getStudyDeploymentIds).
     */
    val participations: Map<UUID, Set<DeanonymizedParticipation>>
        get() = _participations

    private val _participations: MutableMap<UUID, MutableSet<DeanonymizedParticipation>> = mutableMapOf()

    /**
     * Specify that a [Participation] has been created for a [Participant] in this recruitment.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    fun addParticipation( studyDeploymentId: UUID, participation: DeanonymizedParticipation )
    {
        check( canAddParticipations ) { "The study is not yet ready for deployment." }

        _participations
            .getOrPut( studyDeploymentId ) { mutableSetOf() }
            .add( participation )
            .eventIf( true ) { Event.ParticipationAdded( studyDeploymentId, participation ) }
    }

    /**
     * Get all [DeanonymizedParticipation]s for a specific [studyDeploymentId].
     *
     * @throws IllegalArgumentException when the given [studyDeploymentId] is not part of this recruitment.
     */
    fun getParticipations( studyDeploymentId: UUID ): Set<DeanonymizedParticipation>
    {
        val participations: Set<DeanonymizedParticipation> = _participations.getOrElse( studyDeploymentId ) { emptySet() }
        require( participations.isNotEmpty() ) { "The specified study deployment ID is not part of this recruitment." }

        return participations
    }

    /**
     * Get a serializable snapshot of the current state of this [Recruitment].
     */
    override fun getSnapshot(): RecruitmentSnapshot = RecruitmentSnapshot.fromParticipantRecruitment( this )
}
