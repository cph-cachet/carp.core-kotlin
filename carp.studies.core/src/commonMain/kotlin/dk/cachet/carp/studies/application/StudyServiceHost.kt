package dk.cachet.carp.studies.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyDetails
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Implementation of [StudyService] which allows creating and managing studies.
 */
class StudyServiceHost( private val repository: StudyRepository ) : StudyService
{
    /**
     * Create a new study for the specified [owner].
     */
    override suspend fun createStudy(
        owner: StudyOwner,
        /**
         * A descriptive name for the study, assigned by, and only visible to, the [owner].
         */
        name: String,
        /**
         * An optional description of the study, assigned by, and only visible to, the [owner].
         */
        description: String,
        /**
         * An optional description of the study, shared with participants once they are invited.
         * In case no description is specified, [name] is used as the name in [invitation].
         */
        invitation: StudyInvitation?
    ): StudyStatus
    {
        val ensuredInvitation = invitation ?: StudyInvitation( name, "" )
        val study = Study( owner, name, description, ensuredInvitation )

        repository.add( study )

        return study.getStatus()
    }

    /**
     * Set study details which are visible only to the [StudyOwner].
     *
     * @param studyId The id of the study to update the study details for.
     * @param name A descriptive name for the study.
     * @param description A description of the study.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun setInternalDescription( studyId: UUID, name: String, description: String ): StudyStatus
    {
        val study = repository.getById( studyId )
        requireNotNull( study )

        study.name = name
        study.description = description
        repository.update( study )

        return study.getStatus()
    }

    /**
     * Gets detailed information about the study with the specified [studyId], including which study protocol is set.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getStudyDetails( studyId: UUID ): StudyDetails
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        return StudyDetails(
            study.id,
            study.owner,
            study.name,
            study.creationDate,
            study.description,
            study.invitation,
            study.protocolSnapshot
        )
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
        requireNotNull( study )

        return study.getStatus()
    }

    /**
     * Get status for all studies created by the specified [owner].
     */
    override suspend fun getStudiesOverview( owner: StudyOwner ): List<StudyStatus> =
        repository.getForOwner( owner ).map { it.getStatus() }

    /**
     * Specify an [invitation], shared with participants once they are invited to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        study.invitation = invitation
        repository.update( study )

        return study.getStatus()
    }

    /**
     * Specify the study [protocol] to use for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist,
     * when the provided [protocol] snapshot is invalid,
     * or when the protocol contains errors preventing it from being used in deployments.
     * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
     */
    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

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
        requireNotNull( study )

        study.goLive()
        repository.update( study )

        return study.getStatus()
    }
}
