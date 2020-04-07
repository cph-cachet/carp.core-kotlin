package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeployment
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
     * @param studyDeploymentId The id of the [StudyDeployment] to return [StudyDeploymentStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus

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
    suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ): StudyDeploymentStatus

    /**
     * Unregister the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment
     */
    suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus

    /**
     * Get the deployment configuration for the master device with [masterDeviceRoleName] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * or [masterDeviceRoleName] is not present in the deployment, or not yet registered.
     */
    suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ): MasterDeviceDeployment

    /**
     * Indicate to stakeholders in the study deployment with [studyDeploymentId] that the device with [masterDeviceRoleName] was deployed successfully,
     * i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist,
     * or [masterDeviceRoleName] is not present in the deployment or cannot be deployed yet.
     */
    suspend fun deploymentSuccessful( studyDeploymentId: UUID, masterDeviceRoleName: String ): StudyDeploymentStatus

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
    suspend fun addParticipation( studyDeploymentId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ): Participation

    /**
     * Get all participations to study deployments the account with the given [accountId] has been invited to.
     */
    suspend fun getParticipationInvitations( accountId: UUID ): Set<ParticipationInvitation>
}
