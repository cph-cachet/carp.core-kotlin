package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.services.ApplicationServiceEventBus
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.domain.DeploymentRepository
import dk.cachet.carp.deployments.domain.RegistrableDevice
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Instant


/**
 * Application service which allows deploying study protocols to participants
 * and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
class DeploymentServiceHost(
    private val repository: DeploymentRepository,
    private val dataStreamService: DataStreamService,
    private val eventBus: ApplicationServiceEventBus<DeploymentService, DeploymentService.Event>
) : DeploymentService
{
    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot] with participants defined in [invitations].
     *
     * The identities specified in the invitations are used to invite and authenticate the participants.
     * In case no account is associated to an identity, a new account is created for it.
     * An invitation (and account details) is delivered to the person managing the identity,
     * or should be handed out manually to the relevant participant by the person managing the identity.
     *
     * @throws IllegalArgumentException when:
     *  - a deployment with [id] already exists
     *  - [protocol] is invalid
     *  - [invitations] is empty
     *  - any of the assigned device roles in [invitations] is not part of the study [protocol]
     *  - not all master devices part of the study [protocol] have been assigned a participant
     * @return The [StudyDeploymentStatus] of the newly created study deployment.
     */
    override suspend fun createStudyDeployment(
        id: UUID,
        protocol: StudyProtocolSnapshot,
        invitations: List<ParticipantInvitation>,
        /**
         * Optional preregistrations for connected devices in the study [protocol].
         */
        connectedDevicePreregistrations: Map<String, DeviceRegistration>
    ): StudyDeploymentStatus
    {
        protocol.throwIfInvalid( invitations, connectedDevicePreregistrations )

        val newDeployment = StudyDeployment( protocol, id )
        connectedDevicePreregistrations.forEach { (connected, registration) ->
            val device = protocol.connectedDevices.first { it.roleName == connected }
            newDeployment.registerDevice( device, registration )
        }

        repository.add( newDeployment )
        eventBus.publish(
            DeploymentService.Event.StudyDeploymentCreated(
                newDeployment.id,
                newDeployment.protocolSnapshot,
                invitations,
                connectedDevicePreregistrations
            )
        )

        return newDeployment.getStatus()
    }

    /**
     * Remove study deployments with the given [studyDeploymentIds].
     * This also removes all data related to the study deployments managed by [ParticipationService] and [DataStreamService].
     *
     * @return The IDs of study deployments which were removed. IDs for which no study deployment exists are ignored.
     */
    override suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ): Set<UUID>
    {
        val removedIds = repository.remove( studyDeploymentIds )

        dataStreamService.removeDataStreams( studyDeploymentIds )

        eventBus.publish( DeploymentService.Event.StudyDeploymentsRemoved( studyDeploymentIds ) )
        return removedIds
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
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )

        return deployment.getStatus()
    }

    /**
     * Get the statuses for a set of deployments with the specified [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when [studyDeploymentIds] contains an ID for which no deployment exists.
     */
    override suspend fun getStudyDeploymentStatusList( studyDeploymentIds: Set<UUID> ): List<StudyDeploymentStatus> =
        repository
            .getStudyDeploymentsOrThrowBy( studyDeploymentIds )
            .map { it.getStatus() }

    /**
     * Register the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @param registration A matching configuration for the device with [deviceRoleName].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment or is already registered and a different [registration] is specified than a previous request
     * - [registration] is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device
     * @throws IllegalStateException when this deployment has stopped.
     */
    override suspend fun registerDevice(
        studyDeploymentId: UUID,
        deviceRoleName: String,
        registration: DeviceRegistration
    ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )
        val device: AnyDeviceDescriptor = getRegistrableDevice( deployment, deviceRoleName ).device

        // Early out when the device is already registered.
        val priorRegistration = deployment.registeredDevices[ device ]
        if ( !deployment.isStopped && priorRegistration == registration )
        {
            return deployment.getStatus()
        }

        // Register device and save/distribute changes.
        deployment.registerDevice( device, registration )
        repository.update( deployment )
        val registered = DeploymentService.Event.DeviceRegistrationChanged( studyDeploymentId, device, registration )
        eventBus.publish( registered )

        return deployment.getStatus()
    }

    /**
     * Unregister the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment
     * @throws IllegalStateException when this deployment has stopped.
     */
    override suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )
        val device: AnyDeviceDescriptor = getRegistrableDevice( deployment, deviceRoleName ).device

        val isRegistered = device in deployment.registeredDevices.keys
        if ( isRegistered )
        {
            deployment.unregisterDevice( device )
            repository.update( deployment )
            val unregistered = DeploymentService.Event.DeviceRegistrationChanged( studyDeploymentId, device, null )
            eventBus.publish( unregistered )
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
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )
        val device = getRegisteredMasterDevice( deployment, masterDeviceRoleName )

        return deployment.getDeviceDeploymentFor( device )
    }

    /**
     * Indicate to stakeholders in the study deployment with [studyDeploymentId]
     * that the device with [masterDeviceRoleName] was deployed successfully,
     * using the device deployment with timestamp [deviceDeploymentLastUpdatedOn],
     * i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it.
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [masterDeviceRoleName] is not present in the deployment
     * - the [deviceDeploymentLastUpdatedOn] does not match the expected timestamp. The deployment might be outdated.
     * @throws IllegalStateException when the deployment cannot be deployed yet, or the deployment has stopped.
     */
    override suspend fun deploymentSuccessful(
        studyDeploymentId: UUID,
        masterDeviceRoleName: String,
        deviceDeploymentLastUpdatedOn: Instant
    ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )
        val device = getRegisteredMasterDevice( deployment, masterDeviceRoleName )

        deployment.deviceDeployed( device, deviceDeploymentLastUpdatedOn )
        repository.update( deployment )

        val deploymentStatus = deployment.getStatus()

        // Once the deployment is ready, open the required data streams.
        if ( deploymentStatus is StudyDeploymentStatus.DeploymentReady )
        {
            dataStreamService.openDataStreams( deployment.requiredDataStreams )
        }

        return deploymentStatus
    }

    /**
     * Stop the study deployment with the specified [studyDeploymentId].
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    override suspend fun stop( studyDeploymentId: UUID ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )

        if ( !deployment.isStopped )
        {
            // Close all data streams used by the deployment.
            if ( deployment.getStatus() is StudyDeploymentStatus.DeploymentReady )
            {
                dataStreamService.closeDataStreams( setOf( studyDeploymentId ) )
            }

            deployment.stop()
            repository.update( deployment )
            eventBus.publish( DeploymentService.Event.StudyDeploymentStopped( studyDeploymentId ) )
        }

        return deployment.getStatus()
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
