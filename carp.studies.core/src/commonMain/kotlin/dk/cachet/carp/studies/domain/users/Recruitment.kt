package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.application.throwIfInvalid
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.application.users.participantIds


/**
 * Represents a set of [participants] recruited for a study identified by [studyId].
 */
class Recruitment( val studyId: UUID ) :
    AggregateRoot<Recruitment, RecruitmentSnapshot, Recruitment.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class ParticipantAdded( val participant: Participant ) : Event()
        data class ParticipationAdded( val participant: Participant, val studyDeploymentId: UUID ) : Event()
    }


    companion object
    {
        fun fromSnapshot( snapshot: RecruitmentSnapshot ): Recruitment
        {
            val recruitment = Recruitment( snapshot.studyId )
            recruitment.creationDate = snapshot.creationDate
            if ( snapshot.studyProtocol != null && snapshot.invitation != null )
            {
                recruitment.lockInStudy( snapshot.studyProtocol, snapshot.invitation )
            }
            snapshot.participants.forEach { recruitment._participants.add( it ) }
            for ( (deploymentId, participantIds) in snapshot.participations )
            {
                recruitment._participations[ deploymentId ] = participantIds
                    .map { id -> recruitment.participants.first { it.id == id } }
                    .toMutableSet()
            }

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
    fun addParticipant( email: EmailAddress ): Participant
    {
        // Verify whether participant was already added.
        val identity = EmailAccountIdentity( email )
        var participant = _participants.firstOrNull { it.accountIdentity == identity }

        // Add new participant in case it was not added before.
        if ( participant == null )
        {
            participant = Participant( identity )
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
        return if ( protocol != null && invitation != null ) RecruitmentStatus.ReadyForDeployment( protocol, invitation )
            else RecruitmentStatus.AwaitingStudyToGoLive
    }

    /**
     * Attempt creating [ParticipantInvitation]s for the specified participant [group],
     * or throw exception in case preconditions are violated.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     * @throws IllegalArgumentException when:
     *  - any of the participants specified in [group] does not exist
     *  - [group] is empty
     *  - any of the device roles specified in [group] are not part of the configured study protocol
     *  - not all master devices part of the study protocol have been assigned a participant
     */
    fun createInvitations( group: Set<AssignParticipantDevices> ): Pair<StudyProtocolSnapshot, List<ParticipantInvitation>>
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
                toAssign.masterDeviceRoleNames,
                participant.accountIdentity,
                status.invitation
            )
        }
        val protocol = status.studyProtocol
        protocol.throwIfInvalid( invitations )

        return Pair( protocol, invitations )
    }

    /**
     * Per study deployment ID, the set of participants that participate in it.
     */
    val participations: Map<UUID, Set<Participant>>
        get() = _participations

    private val _participations: MutableMap<UUID, MutableSet<Participant>> = mutableMapOf()

    /**
     * Specify that [participant] of this recruitment participates in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when [participant] is not a participant in this recruitment.
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    fun addParticipation( participant: Participant, studyDeploymentId: UUID )
    {
        require( participant in participants ) { "The participant is not part of this recruitment." }
        check( getStatus() is RecruitmentStatus.ReadyForDeployment ) { "The study is not yet ready for deployment." }

        _participations
            .getOrPut( studyDeploymentId ) { mutableSetOf() }
            .add( participant )
            .eventIf( true ) { Event.ParticipationAdded( participant, studyDeploymentId ) }
    }

    /**
     * Get the [ParticipantGroupStatus] of the study deployment identified by [studyDeploymentStatus].
     *
     * @throws IllegalArgumentException when the study deployment identified by [studyDeploymentStatus] is not part of this recruitment.
     */
    fun getParticipantGroupStatus( studyDeploymentStatus: StudyDeploymentStatus ): ParticipantGroupStatus
    {
        val deploymentId = studyDeploymentStatus.studyDeploymentId
        val participants: Set<Participant> = _participations.getOrElse( deploymentId ) { emptySet() }
        require( participations.isNotEmpty() )
            { "A study deployment with ID \"$deploymentId\" is not part of this recruitment." }

        return ParticipantGroupStatus( studyDeploymentStatus, participants )
    }

    /**
     * Get a serializable snapshot of the current state of this [Recruitment].
     */
    override fun getSnapshot(): RecruitmentSnapshot = RecruitmentSnapshot.fromParticipantRecruitment( this )
}
