package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ApplicationService
import dk.cachet.carp.common.ddd.IntegrationEvent
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlinx.serialization.Serializable


/**
 * Application service which allows deploying [StudyProtocol]'s
 * and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
interface DeploymentService : ApplicationService<DeploymentService, DeploymentService.Event>
{
    @Serializable
    sealed class Event : IntegrationEvent<DeploymentService>()
    {
        @Serializable
        data class StudyDeploymentCreated( val deployment: StudyDeploymentSnapshot ) : Event()
        @Serializable
        data class StudyDeploymentStopped( val studyDeploymentId: UUID ) : Event()
    }


    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot].
     *
     * @throws IllegalArgumentException when [protocol] is invalid.
     * @return The [StudyDeploymentStatus] of the newly created study deployment.
     */
    suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ): StudyDeploymentStatus

    /**
     * Get the status for a study deployment with the given [studyDeploymentId].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus

    /**
     * Get the statuses for a set of deployments with the specified [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when [studyDeploymentIds] contains an ID for which no deployment exists.
     */
    suspend fun getStudyDeploymentStatusList( studyDeploymentIds: Set<UUID> ): List<StudyDeploymentStatus>

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
    suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ): StudyDeploymentStatus

    /**
     * Unregister the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment
     * @throws IllegalStateException when this deployment has stopped.
     */
    suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus

    /**
     * Get the deployment configuration for the master device with [masterDeviceRoleName] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [masterDeviceRoleName] is not present in the deployment
     * @throws IllegalStateException when the deployment for the requested master device is not yet available.
     */
    suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ): MasterDeviceDeployment

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
    suspend fun deploymentSuccessful(
        studyDeploymentId: UUID,
        masterDeviceRoleName: String,
        deviceDeploymentLastUpdateDate: DateTime
    ): StudyDeploymentStatus

    /**
     * Stop the study deployment with the specified [studyDeploymentId].
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    suspend fun stop( studyDeploymentId: UUID ): StudyDeploymentStatus
}
