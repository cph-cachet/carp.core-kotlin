package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyDetails
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.deviceRoles
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.participantIds
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Implementation of [StudyService] which allows creating and managing studies.
 */
@Suppress( "TooManyFunctions" ) // TODO: Perhaps split up participation management from main interface.
class StudyServiceHost(
    private val repository: StudyRepository,
    private val deploymentService: DeploymentService
) : StudyService
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

    /**
     * Deploy the study with the given [studyId] to a [group] of previously added participants.
     * In case a group with the same participants has already been deployed and is still running (not stopped),
     * the latest status for this group is simply returned.
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - [group] is empty
     *  - any of the participants specified in [group] does not exist
     *  - any of the device roles specified in [group] are not part of the configured study protocol
     *  - not all devices part of the study have been assigned a participant
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): ParticipantGroupStatus
    {
        require( group.isNotEmpty() ) { "No participants to deploy specified." }

        // Verify whether the study is ready for deployment.
        val study: Study? = repository.getById( studyId )
        requireNotNull( study ) { "Study with the specified studyId is not found." }
        check( study.canDeployToParticipants ) { "Study is not yet ready to be deployed to participants." }
        val protocolSnapshot = study.protocolSnapshot!!

        // Verify whether the master device roles to deploy exist in the protocol.
        val masterDevices = protocolSnapshot.masterDevices.map { it.roleName }.toSet()
        require( group.deviceRoles().all { it in masterDevices } )
            { "One of the specified device roles is not part of the configured study protocol." }

        // Verify whether all master devices in the study protocol have been assigned to a participant.
        require( group.deviceRoles().containsAll( masterDevices ) )
            { "Not all devices required for this study have been assigned to a participant." }

        // In case the same participants have been invited before,
        // and that deployment is still running, return the existing group.
        // TODO: The same participants might be invited for different role names, which we currently cannot differentiate between.
        val toDeployParticipantIds = group.map { it.participantId }.toSet()
        val deployedStatus = study.participations.entries
            .firstOrNull { p -> p.value.map { it.participantId }.toSet() == toDeployParticipantIds }
            ?.let { deploymentService.getStudyDeploymentStatus( it.key ) }
        if ( deployedStatus != null && deployedStatus !is StudyDeploymentStatus.Stopped )
        {
            val participants = study.getParticipations( deployedStatus.studyDeploymentId )
            return ParticipantGroupStatus( deployedStatus, participants )
        }

        // Get participant information.
        val allParticipants = repository.getParticipants( studyId ).associateBy { it.id }
        require( group.participantIds().all { it in allParticipants } )
            { "One of the specified participants is not part of this study." }

        // Create deployment and add participations to study.
        // TODO: How to deal with failing or partially succeeding requests?
        //       In a distributed setup, deploymentService would be network calls.
        val deploymentStatus = deploymentService.createStudyDeployment( protocolSnapshot )
        for ( toAssign in group )
        {
            val identity: AccountIdentity = allParticipants.getValue( toAssign.participantId ).accountIdentity
            val participation = deploymentService.addParticipation(
                deploymentStatus.studyDeploymentId,
                toAssign.deviceRoleNames,
                identity,
                study.invitation )

            study.addParticipation(
                deploymentStatus.studyDeploymentId,
                DeanonymizedParticipation( toAssign.participantId, participation.id ) )
        }

        repository.update( study )

        val participants = study.getParticipations( deploymentStatus.studyDeploymentId )
        return ParticipantGroupStatus( deploymentStatus, participants )
    }

    /**
     * Get the status of all deployed participant groups in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipantGroupStatusList( studyId: UUID ): List<ParticipantGroupStatus>
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study ) { "Study with the specified studyId is not found." }

        // Get study deployment statuses.
        val studyDeploymentIds = study.participations.keys
        val studyDeploymentStatuses = deploymentService.getStudyDeploymentStatusList( studyDeploymentIds )

        // Map each study deployment status to a deanonymized participant group status.
        return studyDeploymentStatuses.map {
            val participants = study.getParticipations( it.studyDeploymentId )
            ParticipantGroupStatus( it, participants )
        }
    }

    /**
     * Stop the study deployment in the study with the given [studyId]
     * of the participant group with the specified [groupId] (equivalent to the studyDeploymentId).
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant group with [groupId] does not exist.
     */
    override suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ): ParticipantGroupStatus
    {
        // We don't really need to verify whether the study exists since groupId is equivalent to studyDeploymentId.
        // However, for future-proofing, if they were to differ, it is good to already enforce the dependence on studyId.
        val study: Study? = repository.getById( studyId )
        requireNotNull( study ) { "Study with the specified studyId is not found." }
        val participations = study.participations.getOrElse( groupId ) { emptySet() }
        require( participations.count() > 0 ) { "Study deployment with the specified groupId not found." }

        val deploymentStatus = deploymentService.stop( groupId )
        return ParticipantGroupStatus( deploymentStatus, participations )
    }
}
