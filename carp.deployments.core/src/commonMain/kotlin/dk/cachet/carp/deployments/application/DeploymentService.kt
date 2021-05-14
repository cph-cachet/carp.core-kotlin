package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


/**
 * Application service which allows deploying study protocols to participants
 * and retrieving [MasterDeviceDeployment]'s for participating master devices as defined in the protocol.
 */
interface DeploymentService : ApplicationService<DeploymentService, DeploymentService.Event>
{
    @Serializable
    sealed class Event : IntegrationEvent<DeploymentService>()
    {
        @Serializable
        data class StudyDeploymentCreated(
            val studyDeploymentId: UUID,
            val protocol: StudyProtocolSnapshot,
            val invitations: List<ParticipantInvitation>,
            val connectedDevicePreregistrations: Map<String, DeviceRegistration>
        ) : Event()
        @Serializable
        data class StudyDeploymentsRemoved( val deploymentIds: Set<UUID> ) : Event()
        @Serializable
        data class StudyDeploymentStopped( val studyDeploymentId: UUID ) : Event()
        @Serializable
        data class DeviceRegistrationChanged(
            val studyDeploymentId: UUID,
            val device: AnyDeviceDescriptor,
            val registration: DeviceRegistration?
        ) : Event()
    }


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
    suspend fun createStudyDeployment(
        id: UUID,
        protocol: StudyProtocolSnapshot,
        invitations: List<ParticipantInvitation>,
        /**
         * Optional preregistrations for connected devices in the study [protocol].
         */
        connectedDevicePreregistrations: Map<String, DeviceRegistration> = emptyMap()
    ): StudyDeploymentStatus

    /**
     * Remove study deployments with the given [studyDeploymentIds].
     * This also removes all data related to the study deployments managed by [ParticipationService].
     *
     * @return The IDs of study deployments which were removed. IDs for which no study deployment exists are ignored.
     */
    suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ): Set<UUID>

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
