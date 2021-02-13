package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ApplicationServiceEventBus
import dk.cachet.carp.deployment.domain.DeploymentRepository
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.RegistrableDevice
import dk.cachet.carp.deployment.domain.StudyDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Application service which allows deploying [StudyProtocol]'s
 * and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
class DeploymentServiceHost(
    private val repository: DeploymentRepository,
    private val eventBus: ApplicationServiceEventBus<DeploymentService, DeploymentService.Event>
) : DeploymentService
{
    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot].
     *
     * @throws IllegalArgumentException when [protocol] is invalid.
     * @return The [StudyDeploymentStatus] of the newly created study deployment.
     */
    override suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ): StudyDeploymentStatus
    {
        val newDeployment = StudyDeployment( protocol )

        repository.add( newDeployment )
        eventBus.publish( DeploymentService.Event.StudyDeploymentCreated( newDeployment.getSnapshot() ) )

        return newDeployment.getStatus()
    }

    /**
     * Remove study deployments with the given [studyDeploymentIds].
     * This also removes all data related to the study deployments managed by [ParticipationService].
     *
     * @return The IDs of study deployments which were removed. IDs for which no study deployment exists are ignored.
     */
    override suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ): Set<UUID>
    {
        val removedIds = repository.remove( studyDeploymentIds )
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
    override suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ): StudyDeploymentStatus
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
     * Indicate to stakeholders in the study deployment with [studyDeploymentId] that the device with [masterDeviceRoleName] was deployed successfully,
     * using the deployment with the specified [deviceDeploymentLastUpdateDate],
     * i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it.
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [masterDeviceRoleName] is not present in the deployment
     * - the [deviceDeploymentLastUpdateDate] does not match the expected date. The deployment might be outdated.
     * @throws IllegalStateException when the deployment cannot be deployed yet, or the deployment has stopped.
     */
    override suspend fun deploymentSuccessful(
        studyDeploymentId: UUID,
        masterDeviceRoleName: String,
        deviceDeploymentLastUpdateDate: DateTime
    ): StudyDeploymentStatus
    {
        val deployment: StudyDeployment = repository.getStudyDeploymentOrThrowBy( studyDeploymentId )
        val device = getRegisteredMasterDevice( deployment, masterDeviceRoleName )

        deployment.deviceDeployed( device, deviceDeploymentLastUpdateDate )
        repository.update( deployment )

        return deployment.getStatus()
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
