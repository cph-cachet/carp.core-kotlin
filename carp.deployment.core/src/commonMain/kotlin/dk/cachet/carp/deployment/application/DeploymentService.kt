package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * Application service which allows deploying [StudyProtocol]'s, registering participations,
 * and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
interface DeploymentService
{
    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot].
     *
     * @throws InvalidConfigurationError when [protocol] is invalid.
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
    suspend fun addParticipation( studyDeploymentId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ): Participation

    /**
     * Get all participations in study deployments the account with the given [accountId] has been invited to.
     */
    suspend fun getParticipationInvitations( accountId: UUID ): Set<ParticipationInvitation>
}
