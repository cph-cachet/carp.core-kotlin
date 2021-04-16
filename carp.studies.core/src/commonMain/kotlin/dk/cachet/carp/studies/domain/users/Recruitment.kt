package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.DeanonymizedParticipation
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.deviceRoles


/**
 * Represents a set of [participants] recruited for a [Study] identified by [studyId].
 */
class Recruitment( val studyId: UUID ) :
    AggregateRoot<Recruitment, RecruitmentSnapshot, Recruitment.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class ParticipantAdded( val participant: Participant ) : Event()
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
                recruitment.lockInStudy( snapshot.studyProtocol, snapshot.invitation )
            }
            snapshot.participants.forEach { recruitment._participants.add( it ) }
            for ( p in snapshot.participations )
            {
                recruitment._participations[ p.key ] = p.value.toMutableSet()
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
     * Verify whether this [Recruitment] is ready for deployment and participant [group] is configured correctly
     * by evaluating preconditions and throwing exceptions in case preconditions are violated.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     * @throws IllegalArgumentException when:
     *  - [group] is empty
     *  - any of the participants specified in [group] does not exist
     *  - any of the device roles specified in [group] are not part of the configured study protocol
     *  - not all devices part of the study have been assigned a participant
     */
    fun verifyReadyForDeployment( group: Set<AssignParticipantDevices> ): RecruitmentStatus.ReadyForDeployment
    {
        val status = getStatus()
        check( status is RecruitmentStatus.ReadyForDeployment )
            { "Study is not yet ready to be deployed to participants." }
        require( group.isNotEmpty() ) { "No participants to deploy specified." }

        // Verify whether the master device roles to deploy exist in the protocol.
        val masterDevices = status.studyProtocol.masterDevices.map { it.roleName }.toSet()
        require( group.deviceRoles().all { it in masterDevices } )
            { "One of the specified device roles is not part of the configured study protocol." }

        // Verify whether all master devices in the study protocol have been assigned to a participant.
        require( group.deviceRoles().containsAll( masterDevices ) )
            { "Not all devices required for this study have been assigned to a participant." }

        return status
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
        check( getStatus() is RecruitmentStatus.ReadyForDeployment ) { "The study is not yet ready for deployment." }

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
