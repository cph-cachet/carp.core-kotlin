package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
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
    var name: String,
    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    val invitation: StudyInvitation = StudyInvitation.empty(),
    val id: UUID = UUID.randomUUID()
)
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            val study = Study( StudyOwner( snapshot.ownerId ), snapshot.name, snapshot.invitation, snapshot.studyId )
            study.creationDate = snapshot.creationDate
            study.protocolSnapshot = snapshot.protocolSnapshot
            study.isLive = snapshot.isLive
            study._participations.addAll( snapshot.participations )

            return study
        }
    }


    /**
     * The date when this study was created.
     */
    var creationDate: DateTime = DateTime.now()
        private set

    /**
     * Get the status (serializable) of this [Study].
     */
    fun getStatus(): StudyStatus = StudyStatus( id, name, creationDate, canDeployToParticipants, isLive )

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
            check( !isLive )

            if ( value != null )
            {
                val protocol = StudyProtocol.fromSnapshot( value )
                require( protocol.isDeployable() )
                    { "The specified protocol contains deployment errors and therefore won't be able to be deployed." }
            }

            field = value
        }

    /**
     * Determines whether a study protocol has been locked in and the study may be deployed to real participants.
     */
    var isLive: Boolean = false
        private set

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalStateException when no study protocol is set yet.
     */
    fun goLive()
    {
        check( protocolSnapshot != null ) { "A study protocol needs to be defined for a study to go live." }
        isLive = true
    }

    /**
     * Determines whether the study in its current state is ready to be deployed to participants.
     */
    val canDeployToParticipants: Boolean get() = isLive

    /**
     * The set of participants and the specific study deployments they participate in for this study.
     */
    val participations: Set<DeanonymizedParticipation>
        get() = _participations

    private val _participations: MutableSet<DeanonymizedParticipation> = mutableSetOf()

    /**
     * Specify that a [Participation] has been created for a [Participant] in this study.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    fun addParticipation( participation: DeanonymizedParticipation )
    {
        check( canDeployToParticipants ) { "The study is not yet ready for deployment." }

        _participations.add( participation )
    }

    /**
     * Get a serializable snapshot of the current state of this [Study].
     */
    fun getSnapshot(): StudySnapshot
    {
        return StudySnapshot.fromStudy( this )
    }
}
