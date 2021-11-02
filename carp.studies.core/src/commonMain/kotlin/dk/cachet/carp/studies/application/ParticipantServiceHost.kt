package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.ParticipationService
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.domain.users.deviceRoles
import dk.cachet.carp.studies.domain.users.participantIds


class ParticipantServiceHost(
    private val studyRepository: StudyRepository,
    private val participantRepository: ParticipantRepository,
    private val deploymentService: DeploymentService,
    private val participationService: ParticipationService
) : ParticipantService
{
    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant
    {
        getStudyOrThrow( studyId )

        // Verify whether participant was already added.
        val identity = EmailAccountIdentity( email )
        var participant = participantRepository.getParticipants( studyId ).firstOrNull { it.accountIdentity == identity }

        // Add new participant in case it was not added before.
        if ( participant == null )
        {
            participant = Participant( identity )
            participantRepository.addParticipant( studyId, participant )
        }

        return participant
    }

    /**
     * Returns a participant of a study with the specified [studyId], identified by [participantId].
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant with [participantId] does not exist.
     */
    override suspend fun getParticipant( studyId: UUID, participantId: UUID ): Participant
    {
        getStudyOrThrow( studyId )

        // Load participant from repository.
        // We don't expect massive amounts of participants for now, so loading all from repo is fine for now.
        val participant = participantRepository.getParticipants( studyId ).firstOrNull { it.id == participantId }
        requireNotNull( participant )

        return participant
    }

    /**
     * Get all [Participant]s for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipants( studyId: UUID ): List<Participant> =
        getStudyOrThrow( studyId ).let { participantRepository.getParticipants( studyId ) }

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
     *  - not all necessary devices part of the study have been assigned a participant
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): ParticipantGroupStatus
    {
        require( group.isNotEmpty() ) { "No participants to deploy specified." }

        // Verify whether the study is ready for deployment.
        val study: Study = getStudyOrThrow( studyId )
        check( study.canDeployToParticipants ) { "Study is not yet ready to be deployed to participants." }
        val protocolSnapshot = study.protocolSnapshot!!

        // Verify whether the master device roles to deploy exist in the protocol.
        val masterDevices = protocolSnapshot.masterDevices
        require( group.deviceRoles().all { assignedRoleName -> assignedRoleName in masterDevices.map { it.roleName } } )
            { "One of the specified device roles is not part of the configured study protocol." }

        // Verify whether all master devices in the study protocol have been assigned to a participant.
        require( group.deviceRoles().containsAll( masterDevices.filter { !it.isOptional }.map { it.roleName } ) )
            { "Not all necessary devices required for this study have been assigned to a participant." }

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
            val participantData = participationService.getParticipantData( deployedStatus.studyDeploymentId )
            return ParticipantGroupStatus( deployedStatus, participants, participantData.data )
        }

        // Get participant information.
        val allParticipants = participantRepository.getParticipants( studyId ).associateBy { it.id }
        require( group.participantIds().all { it in allParticipants } )
            { "One of the specified participants is not part of this study." }

        // Create deployment and add participations to study.
        // TODO: How to deal with failing or partially succeeding requests?
        //       In a distributed setup, deploymentService would be network calls.
        val deploymentStatus = deploymentService.createStudyDeployment( protocolSnapshot )
        for ( toAssign in group )
        {
            val identity: AccountIdentity = allParticipants.getValue( toAssign.participantId ).accountIdentity
            val participation = participationService.addParticipation(
                deploymentStatus.studyDeploymentId,
                toAssign.deviceRoleNames,
                identity,
                study.invitation )

            study.addParticipation(
                deploymentStatus.studyDeploymentId,
                DeanonymizedParticipation( toAssign.participantId, participation.id ) )
        }

        studyRepository.update( study )

        val participants = study.getParticipations( deploymentStatus.studyDeploymentId )
        val participantData = participationService.getParticipantData( deploymentStatus.studyDeploymentId )
        return ParticipantGroupStatus( deploymentStatus, participants, participantData.data )
    }

    /**
     * Get the status of all deployed participant groups in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipantGroupStatusList( studyId: UUID ): List<ParticipantGroupStatus>
    {
        val study: Study = getStudyOrThrow( studyId )

        // Get study deployment statuses.
        val studyDeploymentIds = study.participations.keys
        val studyDeploymentStatuses: List<StudyDeploymentStatus> =
            if ( studyDeploymentIds.isEmpty() ) emptyList()
            else deploymentService.getStudyDeploymentStatusList( studyDeploymentIds )

        // Map each study deployment status to a deanonymized participant group status.
        val participantDataList = participationService.getParticipantDataList( studyDeploymentIds )
        return studyDeploymentStatuses.map { deployment ->
            val participants = study.getParticipations( deployment.studyDeploymentId )
            val participantData = participantDataList.first { it.studyDeploymentId == deployment.studyDeploymentId }
            ParticipantGroupStatus( deployment, participants, participantData.data )
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
        val participations = getStudyParticipationsOrThrow( studyId, groupId )

        val deploymentStatus = deploymentService.stop( groupId )
        val participantData = participationService.getParticipantData( deploymentStatus.studyDeploymentId )
        return ParticipantGroupStatus( deploymentStatus, participations, participantData.data )
    }

    /**
     * Set participant [data] for the given [inputDataType],
     * related to participants of the participant group with [groupId] in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when:
     *   - a study with [studyId] or participant group with [groupId] does not exist.
     *   - [inputDataType] is not configured as expected participant data in the study protocol
     *   - [data] is invalid data for [inputDataType]
     */
    override suspend fun setParticipantGroupData( studyId: UUID, groupId: UUID, inputDataType: InputDataType, data: Data? ): ParticipantGroupStatus
    {
        val participations = getStudyParticipationsOrThrow( studyId, groupId )

        val deploymentStatus = deploymentService.getStudyDeploymentStatus( groupId )
        val newData = participationService.setParticipantData( groupId, inputDataType, data )
        return ParticipantGroupStatus( deploymentStatus, participations, newData.data )
    }

    private suspend fun getStudyOrThrow( studyId: UUID ): Study = studyRepository.getById( studyId )
        ?: throw IllegalArgumentException( "Study with the specified studyId does not exist." )

    private suspend fun getStudyParticipationsOrThrow( studyId: UUID, groupId: UUID ): Set<DeanonymizedParticipation>
    {
        val study: Study = getStudyOrThrow( studyId )
        val participations = study.participations[ groupId ]
        requireNotNull( participations ) { "Study deployment with the specified groupId not found." }

        return participations
    }
}
