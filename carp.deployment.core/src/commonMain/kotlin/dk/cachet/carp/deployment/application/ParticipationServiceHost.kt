package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.CarpInputDataTypes
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.InputDataTypeList
import dk.cachet.carp.common.ddd.ApplicationServiceEventBus
import dk.cachet.carp.common.ddd.subscribe
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.ParticipantData
import dk.cachet.carp.deployment.domain.users.ParticipantGroup
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.ParticipationRepository
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.deployment.domain.users.filterActiveParticipationInvitations


/**
 * Application service which allows inviting participants, retrieving participations for study deployments,
 * and managing data related to participants which is input by users.
 *
 * TODO: Replace dependency on [deploymentRepository] with a dependency on [DeploymentService].
 */
class ParticipationServiceHost(
    private val deploymentRepository: DeploymentRepository,
    private val participationRepository: ParticipationRepository,
    private val accountService: AccountService,
    private val eventBus: ApplicationServiceEventBus<ParticipationService, ParticipationService.Event>,
    /**
     * Supported [InputDataType]'s for participant data input by users.
     */
    private val participantDataInputTypes: InputDataTypeList = CarpInputDataTypes
) : ParticipationService
{
    init
    {
        // Create a ParticipantGroup per study deployment.
        eventBus.subscribe { created: DeploymentService.Event.StudyDeploymentCreated ->
            val group = ParticipantGroup.fromDeployment( created.deployment.toObject() )
            participationRepository.putParticipantGroup( group )
        }
    }


    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId],
     * using the master devices with the specified [deviceRoleNames].
     * In case no account is associated to the specified [identity], a new account is created.
     * An [invitation] (and account details) is delivered to the person managing the [identity],
     * or should be handed out manually to the relevant participant by the person managing the specified [identity].
     *
     * @throws IllegalArgumentException when:
     * - there is no study deployment with [studyDeploymentId]
     * - any of the [deviceRoleNames] are not part of the study protocol deployment
     * @throws IllegalStateException when:
     * - the specified [identity] was already invited to participate in this deployment and a different [invitation] is specified than a previous request
     * - this deployment has stopped
     */
    override suspend fun addParticipation(
        studyDeploymentId: UUID,
        deviceRoleNames: Set<String>,
        identity: AccountIdentity,
        invitation: StudyInvitation
    ): Participation
    {
        val studyDeployment = deploymentRepository.getStudyDeploymentOrThrowBy( studyDeploymentId )
        val masterDeviceRoleNames = studyDeployment.protocol.masterDevices.map { it.roleName }
        require( masterDeviceRoleNames.containsAll( deviceRoleNames ) )

        var account = accountService.findAccount( identity )

        // Retrieve or create participation.
        var participation = account?.let { studyDeployment.getParticipation( it ) }
        val isNewParticipation = participation == null
        participation = participation ?: Participation( studyDeploymentId )

        // Ensure an account exists for the given identity and an invitation has been sent out.
        var invitationSent = false
        val deviceDescriptors = deviceRoleNames.map { roleToUse ->
            studyDeployment.protocol.masterDevices.first { it.roleName == roleToUse } }
        if ( account == null )
        {
            account = accountService.inviteNewAccount( identity, invitation, participation, deviceDescriptors )
            invitationSent = true
        }
        else if ( isNewParticipation )
        {
            accountService.inviteExistingAccount( account.id, invitation, participation, deviceDescriptors )
            invitationSent = true
        }

        // Store the invitation so that users can also query for it later.
        if ( invitationSent )
        {
            val invitation = ParticipationInvitation( participation, invitation, deviceRoleNames )
            participationRepository.addInvitation( account.id, invitation )
        }

        // Add participation to study deployment.
        if ( isNewParticipation )
        {
            studyDeployment.addParticipation( account, participation )
            deploymentRepository.update( studyDeployment )
        }
        else
        {
            // This participation was already added and an invitation has been sent.
            // Ensure the request is the same, otherwise, an 'update' might be expected, which is not supported.
            val previousInvitation = participationRepository.getInvitations( account.id ).first { it.participation.id == participation.id }
            check( previousInvitation.invitation == invitation && previousInvitation.deviceRoleNames == deviceRoleNames )
                { "This person is already invited to participate in this study and the current invite deviates from the previous one." }
        }

        return participation
    }

    /**
     * Get all participations of active study deployments the account with the given [accountId] has been invited to.
     */
    override suspend fun getActiveParticipationInvitations( accountId: UUID ): Set<ActiveParticipationInvitation>
    {
        // Get deployment status for each of the account's invitations.
        val invitations = participationRepository.getInvitations( accountId )
        val deploymentIds = invitations.map { it.participation.studyDeploymentId }.toSet()
        val deployments = deploymentRepository.getStudyDeploymentsBy( deploymentIds )

        return filterActiveParticipationInvitations( invitations, deployments )
    }

    /**
     * Get currently set data for all expected participant data in the study deployment with [studyDeploymentId].
     * Data which is not set equals null.
     *
     * @throws IllegalArgumentException when there is no study deployment with [studyDeploymentId].
     */
    override suspend fun getParticipantData( studyDeploymentId: UUID ): ParticipantData
    {
        val group = participationRepository.getParticipantGroupOrThrowBy( studyDeploymentId )

        return ParticipantData( group.studyDeploymentId, group.data.toMap() )
    }

    /**
     * Get currently set data for all expected participant data for a set of study deployments with [studyDeploymentIds].
     * Data which is not set equals null.
     *
     * @throws IllegalArgumentException when [studyDeploymentIds] contains an ID for which no deployment exists.
     */
    override suspend fun getParticipantDataList( studyDeploymentIds: Set<UUID> ): List<ParticipantData>
    {
        val groups = participationRepository.getParticipantGroupListOrThrow( studyDeploymentIds )

        return groups.map { ParticipantData( it.studyDeploymentId, it.data.toMap() ) }
    }

    /**
     * Set participant [data] for the given [inputDataType] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     *   - there is no study deployment with [studyDeploymentId]
     *   - [inputDataType] is not configured as expected participant data in the study protocol
     *   - [data] is invalid data for [inputDataType]
     * @return All data for the specified study deployment, including the newly set data.
     */
    override suspend fun setParticipantData( studyDeploymentId: UUID, inputDataType: InputDataType, data: Data? ): ParticipantData
    {
        val group = participationRepository.getParticipantGroupOrThrowBy( studyDeploymentId )
        group.setData( participantDataInputTypes, inputDataType, data )
        participationRepository.putParticipantGroup( group )

        return ParticipantData( group.studyDeploymentId, group.data.toMap() )
    }
}
