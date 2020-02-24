package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.Participant


/**
 * Implementation of [StudyService] which allows creating and managing studies.
 */
class StudyServiceHost( private val repository: StudyRepository ) : StudyService
{
    /**
     * Create a new study for the specified [owner].
     *
     * @param name A descriptive name for the study, assigned by, and only visible to, the [owner].
     * @param invitation
     *  An optional description of the study, shared with participants once they are invited.
     *  In case no description is specified, [name] is used as the name in [invitation].
     */
    override suspend fun createStudy( owner: StudyOwner, name: String, invitation: StudyInvitation? ): StudyStatus
    {
        val ensuredInvitation = invitation ?: StudyInvitation( name )
        val study = Study( owner, name, ensuredInvitation )

        repository.add( study )

        return study.getStatus()
    }

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyId] does not exist.
     */
    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus
    {
        val study = repository.getById( studyId )
        require( study != null )

        return study.getStatus()
    }

    /**
     * Get status for all studies created by the specified [owner].
     */
    override suspend fun getStudiesOverview( owner: StudyOwner ): List<StudyStatus> =
        repository.getForOwner( owner ).map { it.getStatus() }

    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant
    {
        // Verify whether participant was already added.
        val identity = EmailAccountIdentity( email )
        var participant = repository.getParticipants( studyId ).firstOrNull { it.accountIdentity == identity }

        // Add new participant in case it was not added before.
        if ( participant == null )
        {
            participant = Participant( identity )
            repository.addParticipant( studyId, participant )
        }

        return participant
    }

    /**
     * Get all [Participant]s for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipants( studyId: UUID ): List<Participant> =
        repository.getParticipants( studyId )

    /**
     * Specify the study [protocol] to use for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist,
     * when the provided [protocol] snapshot is invalid,
     * or when the protocol contains errors preventing it from being used in deployments.
     */
    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        require( study != null )

        // Configure study to use the protocol.
        try { study.protocolSnapshot = protocol }
        catch ( e: InvalidConfigurationError ) { throw IllegalArgumentException( e.message ) }

        repository.update( study )

        return study.getStatus()
    }

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     * @throws IllegalStateException when no study protocol for the given study is set yet.
     */
    override suspend fun goLive( studyId: UUID ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        require( study != null )

        study.goLive()

        return study.getStatus()
    }
}
