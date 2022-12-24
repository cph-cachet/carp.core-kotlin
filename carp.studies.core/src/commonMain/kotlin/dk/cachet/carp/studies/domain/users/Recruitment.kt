package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.throwIfInvalidInvitations
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.application.users.participantIds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


/**
 * Represents a set of [participants] recruited for a study identified by [studyId].
 */
class Recruitment( val studyId: UUID, id: UUID = UUID.randomUUID(), createdOn: Instant = Clock.System.now() ) :
    AggregateRoot<Recruitment, RecruitmentSnapshot, Recruitment.Event>( id, createdOn )
{
    sealed class Event : DomainEvent
    {
        data class ParticipantAdded( val participant: Participant ) : Event()
        data class ParticipantGroupAdded( val participantIds: Set<UUID> ) : Event()
    }


    companion object
    {
        fun fromSnapshot( snapshot: RecruitmentSnapshot ): Recruitment
        {
            val recruitment = Recruitment( snapshot.studyId, snapshot.id, snapshot.createdOn )
            if ( snapshot.studyProtocol != null && snapshot.invitation != null )
            {
                recruitment.lockInStudy( snapshot.studyProtocol, snapshot.invitation )
            }
            snapshot.participants.forEach { recruitment._participants.add( it ) }
            snapshot.participantGroups.forEach { recruitment._participantGroups[ it.key ] = it.value }

            recruitment.wasLoadedFromSnapshot( snapshot )
            return recruitment
        }
    }


    // We don't expect massive amounts of participants, so storing them within recruitment is fine for now.
    private val _participants: MutableSet<Participant> = mutableSetOf()

    /**
     * The participants which are part of this [Recruitment].
     */
    val participants: Set<Participant>
        get() = _participants.toSet()

    /**
     * Add a [Participant] identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     */
    fun addParticipant( email: EmailAddress, id: UUID = UUID.randomUUID() ): Participant
    {
        // Verify whether participant was already added.
        val identity = EmailAccountIdentity( email )
        var participant = _participants.firstOrNull { it.accountIdentity == identity }

        // Add new participant in case it was not added before.
        if ( participant == null )
        {
            participant = Participant( identity, id )
            _participants.add( participant )
            event( Event.ParticipantAdded( participant ) )
        }

        return participant
    }

    private var studyProtocol: StudyProtocolSnapshot? = null
    private var invitation: StudyInvitation? = null

    /**
     * Lock in the [protocol] which participants in this recruitment can participate in,
     * and the [invitation] which is sent to them once they are deployed.
     */
    fun lockInStudy( protocol: StudyProtocolSnapshot, invitation: StudyInvitation )
    {
        check( getStatus() is RecruitmentStatus.AwaitingStudyToGoLive )

        this.studyProtocol = protocol
        this.invitation = invitation
    }

    /**
     * Get the status (serializable) of this [Recruitment].
     */
    fun getStatus(): RecruitmentStatus
    {
        val protocol = studyProtocol
        val invitation = invitation
        val status =
            if ( protocol != null && invitation != null ) RecruitmentStatus.ReadyForDeployment( protocol, invitation )
            else RecruitmentStatus.AwaitingStudyToGoLive

        return status
    }

    /**
     * Attempt creating [ParticipantInvitation]s for the specified participant [group],
     * or throw exception in case preconditions are violated.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     * @throws IllegalArgumentException when:
     *  - any of the participants specified in [group] does not exist
     *  - [group] is empty
     *  - any of the participant roles specified in [group] are not part of the configured study protocol
     *  - not all primary devices part of the study protocol have been assigned a participant
     *  - not all necessary participant roles part of the study have been assigned a participant
     */
    fun createInvitations(
        group: Set<AssignedParticipantRoles>
    ): Pair<StudyProtocolSnapshot, List<ParticipantInvitation>>
    {
        val status = getStatus()
        check( status is RecruitmentStatus.ReadyForDeployment )
            { "Study is not yet ready to be deployed to participants." }

        // Verify participants.
        val allParticipants = participants.associateBy { it.id }
        require( group.participantIds().all { it in allParticipants } )
            { "One of the specified participants is not part of this study." }

        // Verify whether invitations match the requirements of the protocol.
        val invitations = group.map { toAssign ->
            val participant = allParticipants.getValue( toAssign.participantId )
            ParticipantInvitation(
                participant.id,
                toAssign.assignedRoles,
                participant.accountIdentity,
                status.invitation
            )
        }
        val protocol = status.studyProtocol
        protocol.throwIfInvalidInvitations( invitations )

        return Pair( protocol, invitations )
    }

    /**
     * Per study deployment ID, the group of participants that participates in it.
     */
    val participantGroups: Map<UUID, StagedParticipantGroup>
        get() = _participantGroups

    private val _participantGroups: MutableMap<UUID, StagedParticipantGroup> = mutableMapOf()

    /**
     * Create and add the participants identified by [participantIds] as a participant group.
     *
     * @throws IllegalArgumentException when one or more of the participants aren't in this recruitment.
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    fun addParticipantGroup( participantIds: Set<UUID>, id: UUID = UUID.randomUUID() ): StagedParticipantGroup
    {
        require( participants.map { it.id }.containsAll( participantIds ) )
            { "One of the participants for which to create a participant group isn't part of this recruitment." }
        check( getStatus() is RecruitmentStatus.ReadyForDeployment ) { "The study is not yet ready for deployment." }

        val group = StagedParticipantGroup( id )
        group.addParticipants( participantIds )

        _participantGroups[ group.id ] = group
        event( Event.ParticipantGroupAdded( participantIds ) )

        return group
    }

    /**
     * Get the [ParticipantGroupStatus] of the study deployment identified by [studyDeploymentStatus].
     *
     * @throws IllegalArgumentException when the study deployment identified by [studyDeploymentStatus] is not part of this recruitment.
     */
    fun getParticipantGroupStatus( studyDeploymentStatus: StudyDeploymentStatus ): ParticipantGroupStatus
    {
        val deploymentId = studyDeploymentStatus.studyDeploymentId
        val group: StagedParticipantGroup = requireNotNull( _participantGroups[ deploymentId ] )
            { "A study deployment with ID \"$deploymentId\" is not part of this recruitment." }

        val participants = group.participantIds.map { id -> _participants.first { it.id == id } }
        return ParticipantGroupStatus.InDeployment.fromDeploymentStatus( participants.toSet(), studyDeploymentStatus )
    }

    /**
     * Get an immutable snapshot of the current state of this [Recruitment] using the specified snapshot [version].
     */
    override fun getSnapshot( version: Int ): RecruitmentSnapshot =
        RecruitmentSnapshot.fromParticipantRecruitment( this, version )
}
