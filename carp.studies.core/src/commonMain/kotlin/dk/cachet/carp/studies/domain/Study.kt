package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.common.ddd.DomainEvent
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Represents a study which can be pilot tested and eventually 'go live', for which a recruitment goal can be set, and participants can be recruited.
 */
class Study(
    /**
     * The person or group that created this [Study].
     */
    val owner: StudyOwner,
    /**
     * A descriptive name for the study, assigned by, and only visible to, the [StudyOwner].
     */
    name: String,
    /**
     * A description for the study, assigned by, and only visible to, the [StudyOwner].
     */
    description: String = "",
    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    invitation: StudyInvitation = StudyInvitation.empty(),
    val id: UUID = UUID.randomUUID()
) : AggregateRoot<Study, StudySnapshot, Study.Event>()
{
    sealed class Event : DomainEvent()
    {
        data class InternalDescriptionChanged( val name: String, val description: String ) : Event()
        data class InvitationChanged( val invitation: StudyInvitation ) : Event()
        data class ProtocolSnapshotChanged( val protocolSnapshot: StudyProtocolSnapshot? ) : Event()
        data class StateChanged( val isLive: Boolean ) : Event()
        data class ParticipationAdded( val studyDeploymentId: UUID, val participation: DeanonymizedParticipation ) : Event()
    }


    companion object Factory
    {
        fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            val study = Study( StudyOwner( snapshot.ownerId ), snapshot.name, snapshot.description, snapshot.invitation, snapshot.studyId )
            study.creationDate = snapshot.creationDate
            study.protocolSnapshot = snapshot.protocolSnapshot
            study.isLive = snapshot.isLive
            for ( p in snapshot.participations )
            {
                study._participations[ p.key ] = p.value.toMutableSet()
            }

            return study
        }
    }


    /**
     * A descriptive name for the study, assigned by, and only visible to, the [StudyOwner].
     */
    var name: String = name
        set( value )
        {
            field = value
            event( Event.InternalDescriptionChanged( name, description ) )
        }

    /**
     * A description for the study, assigned by, and only visible to, the [StudyOwner].
     */
    var description: String = description
        set( value )
        {
            field = value
            event( Event.InternalDescriptionChanged( name, description ) )
        }

    val canSetInvitation: Boolean get() = !isLive

    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    var invitation: StudyInvitation = invitation
        /**
         * Set a new description of the study, to be shared with participants once they are invited to the study.
         *
         * @throws IllegalStateException when the invitation can no longer be changed since the study went 'live'.
         */
        set( value )
        {
            check( !isLive ) { "Can't change invitation since this study already went live." }

            field = value
            event( Event.InvitationChanged( invitation ) )
        }

    /**
     * Get the status (serializable) of this [Study].
     */
    fun getStatus(): StudyStatus =
        if ( isLive ) StudyStatus.Live( id, name, creationDate, canSetInvitation, canSetStudyProtocol, canDeployToParticipants )
        else StudyStatus.Configuring( id, name, creationDate, canSetInvitation, canSetStudyProtocol, canDeployToParticipants, canGoLive )

    val canSetStudyProtocol: Boolean get() = !isLive

    /**
     * A snapshot of the protocol to use in this study, or null when not yet defined.
     */
    var protocolSnapshot: StudyProtocolSnapshot? = null
        /**
         * Set the protocol to use in this study as defined by an immutable snapshot.
         * Passing 'null' removes the assigned protocol.
         *
         * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
         * @throws InvalidConfigurationError when an invalid protocol snapshot is specified.
         * @throws IllegalArgumentException when a protocol is specified which cannot be deployed (contains deployment errors).
         */
        set( value )
        {
            check( !isLive ) { "Can't set protocol since this study already went live." }
            if ( value != null )
            {
                val protocol = StudyProtocol.fromSnapshot( value )
                require( protocol.isDeployable() )
                    { "The specified protocol contains deployment errors and therefore won't be able to be deployed." }
            }

            field = value
            event( Event.ProtocolSnapshotChanged( value ) )
        }

    /**
     * Determines whether a study protocol has been locked in and the study may be deployed to real participants.
     */
    var isLive: Boolean = false
        private set

    private val canGoLive: Boolean get() = protocolSnapshot != null

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalStateException when no study protocol is set yet.
     */
    fun goLive()
    {
        check( protocolSnapshot != null ) { "A study protocol needs to be defined for a study to go live." }

        if ( !isLive )
        {
            isLive = true
            event( Event.StateChanged( isLive ) )
        }
    }

    /**
     * Determines whether the study in its current state is ready to be deployed to participants.
     */
    val canDeployToParticipants: Boolean get() = isLive

    /**
     * Per study deployment ID, the set of participants that participate in it.
     * TODO: Maybe this should be kept private and be replaced with clearer helper functions (e.g., getStudyDeploymentIds).
     */
    val participations: Map<UUID, Set<DeanonymizedParticipation>>
        get() = _participations

    private val _participations: MutableMap<UUID, MutableSet<DeanonymizedParticipation>> = mutableMapOf()

    /**
     * Specify that a [Participation] has been created for a [Participant] in this study.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    fun addParticipation( studyDeploymentId: UUID, participation: DeanonymizedParticipation )
    {
        check( canDeployToParticipants ) { "The study is not yet ready for deployment." }

        _participations
            .getOrPut( studyDeploymentId ) { mutableSetOf() }
            .add( participation )
            .eventIf( true ) { Event.ParticipationAdded( studyDeploymentId, participation ) }
    }

    /**
     * Get all [DeanonymizedParticipation]s for a specific [studyDeploymentId].
     *
     * @throws IllegalArgumentException when the given [studyDeploymentId] is not part of this study.
     */
    fun getParticipations( studyDeploymentId: UUID ): Set<DeanonymizedParticipation>
    {
        val participations: Set<DeanonymizedParticipation> = _participations.getOrElse( studyDeploymentId ) { emptySet() }
        require( participations.isNotEmpty() ) { "The specified study deployment ID is not part of this study." }

        return participations
    }

    /**
     * Get a serializable snapshot of the current state of this [Study].
     */
    override fun getSnapshot(): StudySnapshot = StudySnapshot.fromStudy( this )
}
