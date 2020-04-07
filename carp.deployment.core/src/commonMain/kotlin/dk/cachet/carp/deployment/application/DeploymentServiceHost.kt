package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.RegistrableDevice
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Application service which allows deploying [StudyProtocol]'s, registering participations,
 * and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
@Suppress( "TooManyFunctions" ) // TODO: Can this be decomposed a bit?
class DeploymentServiceHost( private val repository: DeploymentRepository, private val accountService: AccountService ) : DeploymentService
{
    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot].
     *
     * @throws InvalidConfigurationError when [protocol] is invalid.
     * @return The [StudyDeploymentStatus] of the newly created study deployment.
     */
    override suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ): StudyDeploymentStatus
    {
        val newDeployment = StudyDeployment( protocol )

        repository.add( newDeployment )

        return newDeployment.getStatus()
    }

    /**
     * Get the status for a study deployment with the given [studyDeploymentId].
     *
     * @param studyDeploymentId The id of the [StudyDeployment] to return [StudyDeploymentStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = getStudyDeployment( studyDeploymentId )

        return deployment.getStatus()
    }

    /**
     * Register the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @param studyDeploymentId The id of the [StudyDeployment] to register the device for.
     * @param deviceRoleName The role name of the device in the deployment to register.
     * @param registration A matching configuration for the device with [deviceRoleName].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * [deviceRoleName] is not present in the deployment or is already registered and a different [registration] is specified than a previous request,
     * or [registration] is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device.
     */
    override suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = getStudyDeployment( studyDeploymentId )
        val device: RegistrableDevice = getRegistrableDevice( deployment, deviceRoleName )

        // Early out when the device is already registered.
        val priorRegistration = deployment.registeredDevices[ device.device ]
        if ( priorRegistration == registration )
        {
            return deployment.getStatus()
        }
        else require ( priorRegistration == null )
            { "The device with role name '$deviceRoleName' is already registered with differing registration options." }

        // Register device and save changes.
        deployment.registerDevice( device.device, registration )
        repository.update( deployment )

        return deployment.getStatus()
    }

    /**
     * Unregister the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment
     */
    override suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = getStudyDeployment( studyDeploymentId )
        val device: AnyDeviceDescriptor = getRegistrableDevice( deployment, deviceRoleName ).device

        val isRegistered = device in deployment.registeredDevices.keys
        if ( isRegistered )
        {
            deployment.unregisterDevice( device )
            repository.update( deployment )
        }

        return deployment.getStatus()
    }

    /**
     * Get the deployment configuration for the master device with [masterDeviceRoleName] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [masterDeviceRoleName] is not present in the deployment
     * @throws IllegalStateException when the deployment for the requested master device is not yet available.
     */
    override suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ): MasterDeviceDeployment
    {
        val deployment: StudyDeployment = getStudyDeployment( studyDeploymentId )
        val device = getRegisteredMasterDevice( deployment, masterDeviceRoleName )

        return deployment.getDeviceDeploymentFor( device )
    }

    /**
     * Indicate to stakeholders in the study deployment with [studyDeploymentId] that the device with [masterDeviceRoleName] was deployed successfully,
     * i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it.
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [masterDeviceRoleName] is not present in the deployment
     * @throws IllegalStateException when the deployment cannot be deployed yet.
     */
    override suspend fun deploymentSuccessful( studyDeploymentId: UUID, masterDeviceRoleName: String ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = getStudyDeployment( studyDeploymentId )
        val device = getRegisteredMasterDevice( deployment, masterDeviceRoleName )

        deployment.deviceDeployed( device )
        repository.update( deployment )

        return deployment.getStatus()
    }

    /**
     * Let the person with the specified [identity] participate in the study deployment with [studyDeploymentId],
     * using the master devices with the specified [deviceRoleNames].
     * In case no account is associated to the specified [identity], a new account is created.
     * An [invitation] (and account details) is delivered to the person managing the [identity],
     * or should be handed out manually to the relevant participant by the person managing the specified [identity].
     *
     * @throws IllegalArgumentException in case there is no study deployment with [studyDeploymentId],
     * or when any of the [deviceRoleNames] is not part of the study protocol deployment.
     * @throws IllegalStateException in case the specified [identity] was already invited to participate in this deployment
     * and a different [invitation] is specified than a previous request.
     */
    override suspend fun addParticipation( studyDeploymentId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ): Participation
    {
        val studyDeployment = getStudyDeployment( studyDeploymentId )
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
            repository.addInvitation( account.id, ParticipationInvitation( participation, invitation, deviceRoleNames ) )
        }

        // Add participation to study deployment.
        if ( isNewParticipation )
        {
            studyDeployment.addParticipation( account, participation )
            repository.update( studyDeployment )
        }
        else
        {
            // This participation was already added and an invitation has been sent.
            // Ensure the request is the same, otherwise, an 'update' might be expected, which is not supported.
            val previousInvitation = repository.getInvitations( account.id ).first { it.participation.id == participation.id }
            check( previousInvitation.invitation == invitation && previousInvitation.deviceRoleNames == deviceRoleNames )
                { "This person is already invited to participate in this study and the current invite deviates from the previous one." }
        }

        return participation
    }

    /**
     * Get all participations to study deployments the account with the given [accountId] has been invited to.
     */
    override suspend fun getParticipationInvitations( accountId: UUID ): Set<ParticipationInvitation> =
        repository.getInvitations( accountId )


    private fun getStudyDeployment( studyDeploymentId: UUID ): StudyDeployment
    {
        val deployment: StudyDeployment? = repository.getStudyDeploymentBy( studyDeploymentId )
        require( deployment != null ) { "A deployment with ID '$studyDeploymentId' does not exist." }

        return deployment
    }

    private fun getRegistrableDevice( deployment: StudyDeployment, deviceRoleName: String ): RegistrableDevice
    {
        return deployment.registrableDevices.firstOrNull { it.device.roleName == deviceRoleName }
            ?: throw IllegalArgumentException( "A device with the role name '$deviceRoleName' could not be found in the study deployment." )
    }

    private fun getRegisteredMasterDevice( deployment: StudyDeployment, masterDeviceRoleName: String ): AnyMasterDeviceDescriptor
    {
        val registeredDevice = deployment.registeredDevices.entries.firstOrNull { it.key.roleName == masterDeviceRoleName }?.toPair()
            ?: throw IllegalArgumentException( "The specified device role name is not part of this study deployment or is not yet registered." )

        return registeredDevice.first as? AnyMasterDeviceDescriptor
            ?: throw IllegalArgumentException( "The specified device is not a master device and therefore does not have a deployment configuration." )
    }
}
